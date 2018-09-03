package ru.abr.etp.service;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import ru.abr.etp.model.DataPackage;
import ru.abr.etp.model.VO.Etp;
import ru.abr.etp.model.docs.FundsHoldRs;
import ru.abr.etp.model.fault.GWFault;
import ru.abr.etp.model.fault.GWFaultDetail;
import ru.abr.utils.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


@Service( "dataPackageConsumer")
public class DataPackageConsumerImpl implements DataPackageConsumer {

    static final Logger logger = LogManager.getLogger(DataPackageConsumerImpl.class);

    @Override
    public HttpStatus process(String data, StringBuffer faultString) {

        HttpStatus result = null;
        try {

            Config config = Config.getInstance();

            JAXBContext jaxbContext = JAXBContext.newInstance(DataPackage.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            InputStream is = new ByteArrayInputStream( data.getBytes(StandardCharsets.UTF_8));
            DataPackage dataPackage = (DataPackage) jaxbUnmarshaller.unmarshal( is);
            logger.info( "Получен пакет " + dataPackage.toString());


            //
            // декодируем документ
            //
            byte[] pack = java.util.Base64.getDecoder().decode( dataPackage.getDocument());
            String document = new String( pack, "UTF-8");
            logger.info( document);
            //
            // декодируем подпись
            //
            byte[] signature = Base64.getDecoder().decode( dataPackage.getSignature());

            Signer signer = Signer.getInstance();
            //
            // определяем BankId документа
            //
            byte[] decodedDocument = Base64.getDecoder().decode( dataPackage.getDocument());
            String decoded = new String(decodedDocument, "UTF-8");
            logger.info( "декодированнное содержимое\n " + decoded);
            //
            // анализируем прочитанное содержимое
            //
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse( new InputSource(new StringReader( decoded)));
            doc.getDocumentElement().normalize();
            //
            // извлекаем площадку
            //
            //Node OperatorNameNode = doc.getElementsByTagName( "OperatorName").item( 0);
            //String OperatorName = OperatorNameNode.getFirstChild().getNodeValue();
            //logger.info( "Найден тег " + OperatorNameNode.getNodeName() + "=" + OperatorName);
            String OperatorName = utils.getTagValue( doc, "OperatorName");
            logger.info( "Найден тег OperatorName=" + OperatorName);

            Etp etp = EtpHolder.getInstance().get(OperatorName);
            if( etp != null){
                String checkSign = "OK";
                String skipCrypto = etp.getSkipCrypto();
                String thumpPrint = etp.getThumbPrint();
                if( skipCrypto != null && !"yes".equals( skipCrypto) ){
                    checkSign = signer.checkSign( document, signature, thumpPrint);
                }
                if( !checkSign.equals( "OK") ) {
                    faultString.append( makeFaultString(  new GWFaultDetail( "-53", checkSign)));
                    return HttpStatus.INTERNAL_SERVER_ERROR;
                } else {
                    //
                    // проверяем его по xsd-схеме
                    //
                    FileValidator fileValidator = new FileValidator();

                    String errors = fileValidator.validateFile(config.getXsdProc(), document);
                    logger.info(errors);
                    //
                    // были ошибки
                    //
                    if (!"".equals(errors)) {
                        faultString.append(makeFaultString(new GWFaultDetail("-41", errors)));
                        logger.error("Ошибки при разборе\n" + errors);
                        logger.info(faultString);

                        result = HttpStatus.INTERNAL_SERVER_ERROR;

                    } else {
                        //
                        // получаем список клиентов с остатками
                        //
                        ClientListHolder holder = ClientListHolder.getInstance();
                        //
                        // если это один из 4 запросов на блокировку-разблокировку обработаем его отдельно
                        //
                        ArrayList<FundsHoldRs> list = holder.processReceivedPackage(dataPackage.getTypeDocument(), doc);
                        //
                        // готовимся к выводу информации
                        //
                        String fileNamePrefix = "etp_" + OperatorName + "_from_";
                        if (!list.isEmpty()) {
                            fileNamePrefix = fileNamePrefix + "processed_";
                        }
                        File tempFile = File.createTempFile(fileNamePrefix, ".xml", new File(config.getTempPath()));
                        FileWriter fw = new FileWriter(tempFile);
                        //
                        // записываем сам запрос
                        //
                        fw.write(data);
                        //
                        // файл будет содержать входной запрос и оба ответа, у файла будет немного другая маска для обработки
                        //
                        if (!list.isEmpty()) {
                            //
                            // сформировали квитанцию
                            //
                            String ackAsString = getPackageAsString( list.get( 0));
                            logger.info( "Создана квитанция\n" + ackAsString);
                            //
                            // сформировали бизнес-ответ
                            String busAsString = getPackageAsString( list.get(1));
                            logger.info( "Создан бизнес-ответ\n" + busAsString);
                            //
                            // записываем в файл для обработки АБС
                            //
                            fw.write("\n");
                            fw.write("<ack>" + ackAsString + "</ack>");
                            fw.write("\n");
                            fw.write("<bus>" + busAsString + "</bus>");
                            //
                            // формируем файл с квитанцией для отправки на площадку
                            //
                            File respFile = File.createTempFile("bank_resp1_", ".xml", new File(config.getTempPath()));
                            FileWriter fileWriter = new FileWriter( respFile);
                            fileWriter.write( ackAsString);
                            fileWriter.close();
                            utils.moveTo( respFile, config.getTo_ETP_Path() );
                            Thread.sleep( 2000); // делаем задержку, чтобы файлы имели разное время
                            //
                            // формируем файл с бизнес-ответом для отправки на площадку
                            //
                            respFile = File.createTempFile("bank_resp2_", ".xml", new File(config.getTempPath()));
                            fileWriter = new FileWriter( respFile);
                            fileWriter.write( busAsString);
                            fileWriter.close();
                            utils.moveTo( respFile, config.getTo_ETP_Path() );
                        }
                        fw.close();
                        //
                        // перемещаем файл из временного каталога в выходной
                        //
                        utils.moveTo( tempFile, config.getFrom_ETP_Path() );
                    } //if (!"".equals(errors)) {
                    //
                    // ошибок не было
                    //
                    result = HttpStatus.OK;
                    return result;
                } // checkSign
            } else {
                faultString.append( makeFaultString( new GWFaultDetail( "-100", "Неизвестная площадка " + OperatorName)));
            }  // etp found
        }
        catch (Exception e){
            try {
                e.printStackTrace();
                faultString.append( makeFaultString( new GWFaultDetail( "-100", e.toString())));
                logger.error( "Отправлен ответ с ошибкой", e);
            } catch ( Exception ee){
                logger.error( "Ошибка при обработке прочих ошибок", e);
            }
            logger.error( "Ошибка", e);
            result = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return result;
    }


    public String makeFaultString( GWFaultDetail detail)throws Exception{

        GWFault gwFault = new GWFault();
        gwFault.setFaultString( "DataPower error");
        gwFault.setDetail( detail);

        return utils.makeDocString( gwFault);
    }


    public String getPackageAsString( Object obj)throws Exception{
        String Response = utils.makeDocString( obj);
        String ackEncoded = Base64.getEncoder().encodeToString( Response.getBytes( "UTF-8"));
        DataPackage Package = new DataPackage( obj.getClass().getSimpleName(), ackEncoded,"genereted by RESTService");
        String packageAsString = utils.makeDocString( Package);
        return  packageAsString;
    }

}


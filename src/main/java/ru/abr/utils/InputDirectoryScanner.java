package ru.abr.utils;

import ch.qos.logback.core.net.server.Client;
import com.sun.xml.internal.ws.wsdl.writer.document.OpenAtts;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import ru.CryptoPro.JCP.tools.Array;
import ru.abr.etp.model.VO.Etp;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.StreamSupport;

public class InputDirectoryScanner extends Thread {

    static final Logger logger = LogManager.getLogger(InputDirectoryScanner.class);
    
    private Signer signer;
    private Config config;




    private InputDirectoryScanner(){}

    private static InputDirectoryScanner InputDirectoryScanner = null;

    public static InputDirectoryScanner getInstance() throws Exception{
        if( InputDirectoryScanner == null){
            InputDirectoryScanner = new InputDirectoryScanner();
            InputDirectoryScanner.config = Config.getInstance();
            InputDirectoryScanner.signer = Signer.getInstance();

        }
        return InputDirectoryScanner;
    }

    @Override
    public void run(){
        try {

            while ( true){
                iteration();
                logger.info( "Пауза " + config.getDelay() + " ms");
                Thread.sleep( config.getDelay());
            }

        }
        catch (Exception e){
            logger.error( "Запуск сканирования каталогов", e);
        }
    }

    //
    // один проход по каталогу
    //
    public void iteration(){
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setErrorHandler(
                new DefaultResponseErrorHandler() {
                    public void handleError(ClientHttpResponse response) throws IOException {
                        HttpHeaders responseHeaders = response.getHeaders();
                        HttpStatus responseStatus = response.getStatusCode();
                        InputStream is = response.getBody();
                        String body = null;
                        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = is.read(buffer)) != -1) {
                                result.write(buffer, 0, length);
                            }
                            body = result.toString("UTF-8");
                        }
                        logger.info(responseHeaders);
                        logger.info(responseStatus);
                        logger.info(body);
                        File tempFile = File.createTempFile( "etp_err_", ".xml", new File( config.getFrom_ETP_Path()));
                        FileWriter fw = new FileWriter(tempFile);
                        fw.write( body);
                        fw.close();
                    }
                }
        );


        logger.info( "Входной каталог " + config.getTo_ETP_Path() + ", Маска файлов " + config.getFileMask());
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + config.getFileMask());

        Path dir = Paths.get(config.getTo_ETP_Path());
        try (DirectoryStream<Path> stream = Files.newDirectoryStream( dir, config.getFileMask())) {
            logger.info("Инициализируем dom-модель");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            logger.info( "Начинаем сканирование");

            StreamSupport.stream( stream.spliterator(), false)
                    //
                    // сортируем по времени модификации
                    //
                    .sorted((o1, o2) -> {
                        try {
                            return Files.getLastModifiedTime(o1).compareTo(Files.getLastModifiedTime(o2));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        return 0;
                    })
                    //
                    // берем файлы по заданной маске и ненулевого размера. их уже отпустила АБС
                    //
                    .filter(
                            file -> matcher.matches( file.getFileName()) && ( file.toFile().length() > 0)
                    )
                    //
                    // обрабатываем
                    //
                    .forEach(file -> {
                        try {
                            logger.info( file);
                            switch ( file.getFileName().toString()) {
                              case "bank_CLIENT_LIST.xml":
                                ClientListHolder holder = ClientListHolder.getInstance();
                                holder.readClients( file.toString());
                                break;
                            default:
                                processFile(restTemplate, builder, file);
                            }
                        }
                        catch (Exception e){
                            logger.error( "Ошибка при обработке файла " + file, e);
                        }
                    });

        } catch (Exception e) {
            logger.error( "Возникла ошибка ", e);
        }
    }


private void processFile( RestTemplate restTemplate, DocumentBuilder builder,  Path path)throws Exception {
    RandomAccessFile randomAccessFile = null;
    try {
        ResponseEntity<String> responseEntity = null;
        //
        // читаем файл
        //
        logger.info("читаем файл " + path.toFile());
        randomAccessFile = new RandomAccessFile(path.toFile(), "rw");
        byte[] encoded = new byte[(int) randomAccessFile.length()];
        randomAccessFile.read(encoded);
        String content = new String(encoded, StandardCharsets.UTF_8);
        logger.info("содержимое файла " + content);
        //
        // разбираем содержимое в dom-модель
        //
        Document docPackage = builder.parse(new InputSource(new StringReader(content)));
        docPackage.getDocumentElement().normalize();
        //
        // читаем содержимое траспортного документа
        //
        Node docNode = docPackage.getElementsByTagName("Document").item(0);
        //
        // извлекаем содержимое тега Document
        //
        String document = utils.getTagValue(docPackage, "Document");
        logger.info("содержимое тега Document:\n" + document);
        //
        // снимаем кодировку тега Document
        //
        byte[] decodedDocument = Base64.getDecoder().decode(document);
        String decoded = new String(decodedDocument, "UTF-8");
        logger.info("декодированнное содержимое\n=========================\n"
                + decoded +
                "\n=================");
        //
        // анализируем прочитанное содержимое
        //
        Document doc = builder.parse(new InputSource(new StringReader(decoded)));
        doc.getDocumentElement().normalize();
        String signature = signer.sign(decoded);
        //
        // кодируем заново и снова записываем
        //
        byte[] codedDocument = Base64.getEncoder().encode(decodedDocument);
        if (document.equals(codedDocument)) {
            logger.info("Двойное преобразование успешно");
        }

        docNode.getFirstChild().setNodeValue(new String(codedDocument, "UTF-8"));
        //
        // записываем подпись
        //
        Node signNode = docPackage.getElementsByTagName("Signature").item(0);
        signNode = signNode.getFirstChild();
        signNode.setNodeValue(signature);
        //
        // выводим результат
        //
        String docStr = utils.convertDocToString(docPackage);
        String signedFile = config.getTempPath() + "\\" + path.toFile().getName() + ".signed";
        Array.writeFile(signedFile, docStr.getBytes("UTF-8"));
        logger.info(docStr);
        //
        // упаковываем документ в строку и отправляем на сервер
        //
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = new MediaType("text", "xml", StandardCharsets.UTF_8);
        headers.setContentType(mediaType);
        //
        // извлекаем площадку
        //
        String operatorNameID = utils.getTagValue(doc, "OperatorName");
        //
        // url площадки
        //
        Etp etp = EtpHolder.getInstance().get(operatorNameID);
        String url = etp.getUrl();
        if (url == null) {
            logger.error("Не определен url для площадки: " + operatorNameID);
        } else {
            logger.info("Определен url: " + url);

            HttpEntity<String> entity = new HttpEntity<String>(docStr, headers);
            if ("yes".equals(etp.getSend())) {
                logger.info("Запускаем отправку -> " + url);
                responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                if (responseEntity.getBody() != null) {
                    logger.info("Получен ответ " + responseEntity.getBody().toString());
                }
                if (responseEntity.getBody() != null && !"OK".equals(responseEntity.getBody().toString())) {
                    File outFileName = File.createTempFile("etp_" + operatorNameID + "OUT_", ".xml", new File(config.getFrom_ETP_Path()));
                    Files.write( outFileName.toPath(), responseEntity.getBody().getBytes( "UTF-8"));
                }
            }
            logger.info(responseEntity);
        }
    }
    catch ( Exception e){
        logger.error( "Возникла ошибка ", e);
    }
    finally {
        if ( randomAccessFile != null) {
            randomAccessFile.close();
            path.toFile().delete();
        }
    }
}


/*
    //
    // один проход по каталогу
    //
    public void iteration_old(){
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = null;

        restTemplate.setErrorHandler(
                new DefaultResponseErrorHandler() {
                    public void handleError(ClientHttpResponse response) throws IOException {
                        HttpHeaders responseHeaders = response.getHeaders();
                        HttpStatus responseStatus = response.getStatusCode();
                        InputStream is = response.getBody();
                        String body = null;
                        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = is.read(buffer)) != -1) {
                                result.write(buffer, 0, length);
                            }
                            body = result.toString("UTF-8");
                        }
                        logger.info(responseHeaders);
                        logger.info(responseStatus);
                        logger.info(body);
                        File tempFile = File.createTempFile( "etp_err_", ".xml", new File( config.getFrom_ETP_Path()));
                        FileWriter fw = new FileWriter(tempFile);
                        fw.write( body);
                        fw.close();
                    }
                }
        );


        FileChannel fileChannel = null;
        FileLock fileLock = null;
        RandomAccessFile randomAccessFile = null;

        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;
        try {
            logger.info("Инициализируем dom-модель");
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        }catch ( Exception e){
            logger.error("Не удалось инициализировать dom-модель", e);
            return;
        }

        logger.info( "Начинаем сканировать входной каталог " + config.getTo_ETP_Path());
        logger.info( "Ищем файлы по маске " + config.getFileMask());

        Path dir = null;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream( dir, config.getFileMask())) {

            for (Path path : stream) {
                logger.info(path);
                try {
                    logger.info( " читаем файл " + path.toFile());
                    randomAccessFile = new RandomAccessFile(path.toFile(), "rw");

                    fileChannel = randomAccessFile.getChannel();
                    fileLock = fileChannel.tryLock();
                    logger.info( " блокируем файл " + path.toFile());

                    if( fileLock != null){

                        byte[] encoded = new byte[(int) randomAccessFile.length()];
                        logger.info( "читаем файл");
                        randomAccessFile.read( encoded);
                        String content = new String(encoded, StandardCharsets.UTF_8);
                        logger.info( "прочитан файл " + content);
                        //
                        // читаем содержимое
                        //
                        Document docPackage = builder.parse( new InputSource(new StringReader( content)));
                        docPackage.getDocumentElement().normalize();
                        //
                        // читаем содержимое траспортного документа
                        //
                        Node docNode = docPackage.getElementsByTagName("Document").item( 0);
                        //
                        // извлекаем содержимое тега Document
                        //
                        logger.info("\nCurrent Element :" + docNode.getNodeName());
                        String document =  utils.getTagValue( docPackage, "Document");
                        logger.info("содержимое тега Document:\n" + document);
                        //
                        // снимаем кодировку тега Document
                        //
                        byte[] decodedDocument = Base64.getDecoder().decode( document);
                        String decoded = new String(decodedDocument, "UTF-8");
                        logger.info( "декодированнное содержимое\n=========================\n"
                                + decoded +
                                "\n=================");
                        //
                        // анализируем прочитанное содержимое
                        //
                        Document doc = builder.parse( new InputSource(new StringReader( decoded)));
                        doc.getDocumentElement().normalize();
                        String signature = signer.sign( decoded);
                        //
                        // кодируем заново и снова записываем
                        //
                        byte[] codedDocument = Base64.getEncoder().encode( decodedDocument);
                        if( document.equals( codedDocument)){
                            logger.info( "Двойное преобразование успешно");
                        }

                        docNode.getFirstChild().setNodeValue( new String( codedDocument, "UTF-8"));
                        //
                        // записываем подпись
                        //
                        Node signNode = docPackage.getElementsByTagName( "Signature").item( 0);
                        signNode = signNode.getFirstChild();
                        signNode.setNodeValue( signature);
                        //
                        // выводим результат
                        //
                        String docStr = utils.convertDocToString( docPackage);
                        String signedFile = config.getTempPath() + "\\" + path.toFile().getName() + ".signed";
                        Array.writeFile( signedFile, docStr.getBytes( "UTF-8"));
                        logger.info( docStr);
                        //
                        // упаковываем документ в строку и отправляем на сервер
                        //
                        HttpHeaders headers = new HttpHeaders();
                        MediaType mediaType = new MediaType("text", "xml", StandardCharsets.UTF_8);
                        headers.setContentType(mediaType);
                        //
                        // извлекаем площадку
                        //
                        //Node operatorNameNode = doc.getElementsByTagName( "OperatorName").item( 0);
                        //String operatorNameID = operatorNameNode.getFirstChild().getNodeValue();
                        //logger.info( "Найден тег " + operatorNameNode.getNodeName() + "=" + operatorNameID );
                        String operatorNameID = utils.getTagValue( doc, "OperatorName") ;
                        //
                        // url площадки
                        //
                        Etp etp = EtpHolder.getInstance().get( operatorNameID);
                        String url = etp.getUrl();
                        if( url == null ){
                            logger.info( "Не определен url для площадки: " + operatorNameID);
                        }
                        else {
                            logger.info("Определен url: " + url);

                            HttpEntity<String> entity = new HttpEntity<String>(docStr, headers);
                            if ( "yes".equals( etp.getSend())) {
                                logger.info( "Запускаем отправку -> " + url);
                                responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                                if( responseEntity.getBody() != null){
                                    logger.info( "Получен ответ " + responseEntity.getBody().toString());
                                }
                                if ( responseEntity.getBody() != null &&  !"OK".equals( responseEntity.getBody().toString())) {
                                    File outFileName = File.createTempFile("etp_" + operatorNameID + "OUT_", ".xml", new File(config.getFrom_ETP_Path()));
                                    FileWriter fw = new FileWriter(outFileName);
                                    fw.write(responseEntity.getBody());
                                    fw.flush();
                                    fw.close();
                                }
                            }
                            logger.info(responseEntity);
                        }
                    }
                    else {
                        logger.info( "не удалось заблокировать " + path.toFile());
                    }
                }
                catch ( Exception e){
                    logger.error( "Возникла ошибка ", e);
                }
                finally {
                    if ( fileLock != null && fileLock.isValid()) fileLock.release();
                    if ( randomAccessFile != null) {
                        randomAccessFile.close();
                        path.toFile().delete();
                    }
                }
            }
        } catch (Exception e) {
            logger.error( "Возникла ошибка ", e);
        }
    }
*/

}




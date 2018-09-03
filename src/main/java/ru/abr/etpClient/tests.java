package ru.abr.etpClient;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import ru.CryptoPro.JCP.tools.Array;
import ru.abr.etp.model.DataPackage;
import ru.abr.etp.model.VO.Etp;
import ru.abr.etp.model.fault.GWFault;
import ru.abr.etp.model.fault.GWFaultDetail;
import ru.abr.etp.service.DataPackageConsumerImpl;
import ru.abr.utils.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class tests {
    static final Logger logger = LogManager.getLogger(InputDirectoryScanner.class);


    public static void main(String[] args) throws Exception{


        // DirectoryStream<Path> stream = Files.newDirectoryStream( Paths.get( "C:\\Java\\etp\\files\\from_etp" ), "etp.xml" );
        File dir = new File( "C:\\Java\\etp\\files\\from_etp");
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:etp*.xml"  );
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                Path p = Paths.get( name);
                boolean result = matcher.matches( p);
                System.out.println( name + " ->" + result );
                return result;
            }
        });

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                try {
                    return Files.getLastModifiedTime(o1.toPath()).compareTo(Files.getLastModifiedTime(o2.toPath()));
                }
                catch ( IOException e){ return 0;}
            }
        });

        for ( File f: files ) {
            System.out.println( f + "  ->" + Files.getLastModifiedTime( f.toPath()));
        }

        System.out.println( "=================================================================");
        try (DirectoryStream<Path> files2 = Files.newDirectoryStream(dir.toPath())) {
            StreamSupport.stream(files2.spliterator(), false)
                    .sorted((o1, o2) -> {
                        try {
                            return Files.getLastModifiedTime(o1).compareTo(Files.getLastModifiedTime(o2));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        return 0;
                    })
                    .filter(
                            file -> matcher.matches( file.getFileName())
                    )
                    .forEach(file -> {
                        try {
                            System.out.println(file.getFileName() + " ->" + Files.getLastModifiedTime(file));
                        }
                        catch (Exception e){}
                    });
        }

/*

                    .filter(
                            file -> matcher.matches( file)
                    )

 */
        tests t = new tests();
        System.out.println( t.getClass().getName());

        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<tns:Package xmlns:tns=\"http://www.sberbank.ru/edo/oep/edo-oep-document\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <tns:TypeDocument>FundsHoldRq</tns:TypeDocument>\n" +
                "    <tns:Document>PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPEZ1bmRzSG9sZFJxIHhtbG5zPSJodHRwOi8vd3d3LnNiZXJiYW5rLnJ1L2Vkby9vZXAvZWRvLW9lcC1wcm9jIj4KICAgIDxNc2dJRD45MWViNDc5NDRhZDI0Yjg3YmVkZTk5OTcxZmQ5ODQyODwvTXNnSUQ+CiAgICA8TXNnVG0+MjAxOC0wOC0yOVQxMDoxOToyNy44MTArMDM6MDA8L01zZ1RtPgogICAgPE9wZXJhdG9yTmFtZT5FVFBfVEVLVE9SRzwvT3BlcmF0b3JOYW1lPgogICAgPEFwcElEPnRlc3QtMTAxMDwvQXBwSUQ+CiAgICA8QmFua0lEPlJPU1NJWUE8L0JhbmtJRD4KICAgIDxFbnRyeUNsb3NlVG0+MjAxOC0xMi0xN1QwOTozMDo0Ny4wKzAzOjAwPC9FbnRyeUNsb3NlVG0+CiAgICA8TmFtZT7QntCe0J4gJnF1b3Q70JTQldCX0JjQndCi0JXQk9Cg0JDQotCe0KAmcXVvdDs8L05hbWU+CiAgICA8SU5OPjc4MjUxMTk5OTk8L0lOTj4KICAgIDxLUFA+Nzg0MTAxMDAxPC9LUFA+CiAgICA8QWNjb3VudD40MDcwMjgxMDAwMDAwMDAwMDg4MzwvQWNjb3VudD4KICAgIDxBbW91bnRUb0hvbGR4MTAwPjEyMzQ1NjwvQW1vdW50VG9Ib2xkeDEwMD4KPC9GdW5kc0hvbGRScT4=</tns:Document>\n" +
                "    <tns:Signature>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</tns:Signature>\n" +
                "</tns:Package>\n";

        StringBuffer buffer = new StringBuffer();

        ClientListHolder holder = ClientListHolder.getInstance();
        holder.readClients( "C:\\Java\\etp\\files\\to_etp\\bank_CLIENT_LIST.xml");

        DataPackageConsumerImpl consumer = new DataPackageConsumerImpl();
        consumer.process( data, buffer);


        /*

        String fileName = "C:\\Java\\etp\\files\\temp\\ETP_AVK\\bank_ETP_AVK_OUT_16994173371.xml.signed";


        String data = new String(Files.readAllBytes(Paths.get(fileName)));

        JAXBContext jaxbContext = JAXBContext.newInstance(DataPackage.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        InputStream is = new ByteArrayInputStream( data.getBytes(StandardCharsets.UTF_8));
        DataPackage dataPackage = (DataPackage) jaxbUnmarshaller.unmarshal( is);
        logger.info( "Получен пакет " + dataPackage.toString());

        Signer signer = Signer.getInstance();


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

        EtpHolder.getInstance();
        Etp etp = EtpHolder.get( "ETP_AVK");

        String checkSign = signer.checkSign( document, signature, etp.getThumbPrint());

        */


/*

       // DataPackageConsumerImpl packageConsumer = new DataPackageConsumerImpl();
       // GWFaultDetail detail = new GWFaultDetail( "53", "test");
       // String str = packageConsumer.makeFaultString( detail);
       // System.out.println( str);
        RandomAccessFile randomAccessFile = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        try {
            randomAccessFile = new RandomAccessFile("C:\\Java\\etp\\files\\to_etp\\bank_TEKTORG_16990760633.xml", "rw");


                byte[] encoded = new byte[(int) randomAccessFile.length()];
                logger.info( "читаем файл");
                randomAccessFile.read( encoded);
                String content = new String(encoded, StandardCharsets.UTF_8);
                logger.info( "прочитан файл " + content);


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
                String document =  docNode.getFirstChild().getNodeValue();
                logger.info("содержимое тега Document:\n" + document);
                //
                // снимаем кодировку тега Document
                //
                byte[] decodedDocument = Base64.getDecoder().decode( document.getBytes( "UTF-8"));
                String decoded = new String(decodedDocument, "UTF-8");
                logger.info( "декодированнное содержимое\n " + decoded);
                //
                // анализируем прочитанное содержимое
                //
                Document doc = builder.parse( new InputSource(new StringReader( decoded)));
                doc.getDocumentElement().normalize();
                //
                // извлекаем площадку
                //
                Node bankNode = doc.getElementsByTagName( "BankID").item( 0);
                String bankID = bankNode.getFirstChild().getNodeValue();
                logger.info( "Найден тег " + bankNode.getNodeName() + "=" + bankID );
                //
                // url площадки
                //
                //String signature = signer.sign( decoded);
                //
                // записываем подпись
                //
                Node signNode = docPackage.getElementsByTagName( "Signature").item( 0);
                signNode = signNode.getFirstChild();
                //signNode.setNodeValue( signature);
                //
                // выводим результат
                //
                //String docStr = convertDocToString( docPackage);
                ///String signedFile = tempPath + "\\" + path.toFile().getName() + ".signed";
                //Array.writeFile( signedFile, docStr.getBytes( "UTF-8"));
                //logger.info( docStr);
                //
                // упаковываем документ в строку и отправляем на сервер
                //
                HttpHeaders headers = new HttpHeaders();
                MediaType mediaType = new MediaType("text", "xml", StandardCharsets.UTF_8);
                headers.setContentType(mediaType);

        }
        catch ( Exception e){
            e.printStackTrace();
        }
        finally {
             if ( randomAccessFile != null) randomAccessFile.close();
        }

*/
    }

}

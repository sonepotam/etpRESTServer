package ru.abr.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import ru.abr.etp.model.fault.GWFault;
import ru.abr.etp.model.fault.GWFaultDetail;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class utils {

    public static String convertDocToString(final Document document) throws Exception
    {
        DOMSource domSource = new DOMSource(document);
        StringWriter stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        transformer = transformerFactory.newTransformer();
        transformer.transform(domSource, streamResult);
        return stringWriter.toString();
    }


    static public String readFile(String path, String encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static String getTagValue( Document doc, String tagName){
        Node node = doc.getElementsByTagName( tagName).item( 0);
        String result = node.getFirstChild().getNodeValue();
        return result;
    }

    public static String makeDocString ( Object obj)throws Exception{


        JAXBContext jaxbContext = JAXBContext.newInstance( obj.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jaxbMarshaller.marshal( obj, baos);
        String result = new String( baos.toByteArray(), "UTF-8");

        return result;
    }

    public static void moveTo(File file, String dest) throws IOException{
        File newFile = new File( dest + "\\" + file.getName());
        Files.move( file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING );
    }


}

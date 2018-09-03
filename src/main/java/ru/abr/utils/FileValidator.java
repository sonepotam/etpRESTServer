package ru.abr.utils;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.StringReader;

public class FileValidator {
   public static java.lang.String validateFile( String schemaFileName, java.lang.String xml) {
        SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = factory.newSchema(new File(schemaFileName));
            Validator validator = schema.newValidator();
            SoftErrorHandler softErrorHandler = new SoftErrorHandler();
            validator.setErrorHandler(softErrorHandler);
            Source source = new StreamSource( new StringReader( xml));
            validator.validate(source);
            return softErrorHandler.getErrors();
        }
        catch ( Exception e){
            return e.getMessage();
        }
    }
   static class SoftErrorHandler implements ErrorHandler {
      private StringBuilder stringBuffer;
      SoftErrorHandler(){
          stringBuffer = new StringBuilder ();
      }
      public String getErrors(){
         return stringBuffer.toString();
      }
      private String convertStr( String str){
         int ptr = str.indexOf( "lineNumber");
         if( ptr > 0) {
             str = str.substring( ptr);
         }
         return str;
      }
      public void fatalError( SAXParseException e ) throws SAXException {
         stringBuffer.append(convertStr(e.toString())).append( "\n");
      }
      public void error( SAXParseException e ) throws SAXException {
         stringBuffer.append(convertStr(e.toString())).append("\n");
      }
      public void warning( SAXParseException e ) throws SAXException {
         stringBuffer.append(convertStr(e.toString())).append("\n");
       }
    }
}

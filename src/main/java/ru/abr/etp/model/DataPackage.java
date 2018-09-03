package ru.abr.etp.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;


@XmlRootElement( name = "Package", namespace = "http://www.sberbank.ru/edo/oep/edo-oep-document")
@XmlAccessorType(XmlAccessType.NONE)
public class DataPackage implements Serializable
{
    private static final long serialVersionUID = 1L;


    @XmlElement( name = "TypeDocument")
    private String typeDocument;

    @XmlElement( name = "Document")
    private String document;

    @XmlElement( name = "Signature")
    private String signature;

    public DataPackage() { }


    public DataPackage(String typeDocument, String document, String signature) {
        this.typeDocument = typeDocument;
        this.document = document;
        this.signature = signature;
    }


    public String getTypeDocument() {
        return typeDocument;
    }

    public void setTypeDocument(String typeDocument) {
        this.typeDocument = typeDocument;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }


    @Override
    public String toString() {
        return "DataPackage{" +
                "typeDocument='" + typeDocument + '\'' +
                ", document='" + document + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}

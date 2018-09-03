package ru.abr.etp.model.docs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Status implements Serializable {

    private static final long serialVersionUID = 1L;


    @XmlElement( name = "StatusCode")
    int StatusCode;

    @XmlElement( name = "StatusDesc")
    String StatusDesc;


    public Status() {}

    public Status(int statusCode, String statusDesc) {
        StatusCode = statusCode;
        StatusDesc = statusDesc;
    }


    public int getStatusCode() { return StatusCode; }
    public void setStatusCode(int statusCode) { StatusCode = statusCode; }
    public String getStatusDesc() { return StatusDesc; }
    public void setStatusDesc(String statusDesc) { StatusDesc = statusDesc; }

    @Override
    public String toString() {
        return "Status{" +
                "StatusCode=" + StatusCode +
                ", StatusDesc='" + StatusDesc + '\'' +
                '}';
    }
}

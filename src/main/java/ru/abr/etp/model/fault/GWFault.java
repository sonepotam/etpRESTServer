package ru.abr.etp.model.fault;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement( name = "GWFault", namespace = "http://www.sbrf.ru/edo/gateway/common-0.1" )
@XmlAccessorType(XmlAccessType.NONE)
public class GWFault implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement( name = "MsgID")
    private String msgID;

    @XmlElement( name = "MsgTm")
    private String msgTm;

    @XmlElement( name = "faultstring")
    private String faultString;

    @XmlElement( name = "detail")
    private GWFaultDetail detail;



    public GWFault(String msgID, String msgTm, String faultString, GWFaultDetail detail) {
        this.msgID = msgID;
        this.msgTm = msgTm;
        this.faultString = faultString;
    }

    public GWFault() {
    }

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public String getMsgTm() {
        return msgTm;
    }

    public void setMsgTm(String msgTm) {
        this.msgTm = msgTm;
    }

    public String getFaultString() {
        return faultString;
    }

    public void setFaultString(String faultString) {
        this.faultString = faultString;
    }

    public GWFaultDetail getDetail() {
        return detail;
    }

    public void setDetail(GWFaultDetail detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "GWFault{" +
                "msgID='" + msgID + '\'' +
                ", msgTm='" + msgTm + '\'' +
                ", faultString='" + faultString + '\'' +
                '}';
    }


    // TODO : добавить тип detail с описанием ошибки
    // detail
    //code xsd:string
    //[maxLength="10"]
    //Код ошибки шлюза [1]
    //description xsd:string
    //[maxLength="255"]

}



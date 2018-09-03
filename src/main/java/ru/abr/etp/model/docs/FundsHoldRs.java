package ru.abr.etp.model.docs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;


@XmlRootElement( name = "FundsHoldRs", namespace = "http://www.sberbank.ru/edo/oep/edo-oep-proc" )
@XmlAccessorType(XmlAccessType.NONE)
public class FundsHoldRs implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement( name = "MsgID")
    private String msgID;

    @XmlElement( name = "MsgTm")
    private String msgTm;

    @XmlElement( name = "CorrelationID")
    private String correlationID;

    @XmlElement( name = "OperatorName")
    private String operatorName;


    @XmlElement( name = "AppID")
    private String appID;


    @XmlElement( name = "BankID")
    private String bankID;

    @XmlElement( name = "Status")
    private Status status;


    public FundsHoldRs() { }

    public FundsHoldRs(String msgID, String msgTm, String correlationID, String operatorName, String appID, String bankID, Status status) {
        this.msgID = msgID;
        this.msgTm = msgTm;
        this.correlationID = correlationID;
        this.operatorName = operatorName;
        this.appID = appID;
        this.bankID = bankID;
        this.status = status;
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

    public String getCorrelationID() {
        return correlationID;
    }

    public void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getBankID() {
        return bankID;
    }

    public void setBankID(String bankID) {
        this.bankID = bankID;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}


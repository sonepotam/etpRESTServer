package ru.abr.etp.model.VO;

import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

public class PackageVO {
    private String msgID;
    private String msgTm;
    private String typeDoc;
    private String fileName;
    private int summa;

    public PackageVO(String msgID, String msgTm, String typeDoc, String fileName, int summa) {
        this.msgID = msgID;
        this.msgTm = msgTm;
        this.typeDoc = typeDoc;
        this.fileName = fileName;
        this.summa = summa;
    }

    public PackageVO() {
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

    public String getTypeDoc() {
        return typeDoc;
    }

    public void setTypeDoc(String typeDoc) {
        this.typeDoc = typeDoc;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getSumma() {
        return summa;
    }

    public void setSumma(int summa) {
        this.summa = summa;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageVO packageVO = (PackageVO) o;
        return Objects.equals(msgID, packageVO.msgID) &&
                Objects.equals(msgTm, packageVO.msgTm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msgID, msgTm);
    }
}
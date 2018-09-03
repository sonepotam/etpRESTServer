package ru.abr.etp.model.VO;


/*
ETP_RTS.url=http://trunk.rts-tender.ru:9012/bank/integration/package
#ETP_AVK.cert=C:\\Java\\etp\\KEYS\\ETP_AVK\\etp_avk.cer
#ETP_AVK.skypCrypto=no
 */
public class Etp {
    private String url;
    private String thumbPrint;
    private String skipCrypto;
    private String send;


    public Etp() {
        url = "";
        thumbPrint = "";
        skipCrypto = "no";
        send = "yes";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbPrint() {
        return thumbPrint;
    }

    public void setThumbPrint(String thumbPrint) {
        this.thumbPrint = thumbPrint;
    }

    public String getSkipCrypto() {
        return skipCrypto;
    }

    public void setSkipCrypto(String skipCrypto) {
        this.skipCrypto = skipCrypto;
    }

    public String getSend() {
        return send;
    }

    public void setSend(String send) {
        this.send = send;
    }
}

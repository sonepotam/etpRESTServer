package ru.abr.etp.model.VO;


import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlRootElement( name="clientList")
@XmlAccessorType(XmlAccessType.NONE)
public class EtpClientList {

    @XmlElement( name = "client")
    private ArrayList<EtpClient> list;

    public EtpClientList(){
        list = new ArrayList<>();
    }

    public void setEtpClientList(ArrayList<EtpClient> etpClientList) {
        this.list = etpClientList;
    }

    public ArrayList<EtpClient> getEtpClientList() {
        return list;
    }


}

package ru.abr.etp.model.fault;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class GWFaultDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement( name = "code")
    private String code;

    @XmlElement( name = "description")
    private String description;

    public GWFaultDetail() {}

    public GWFaultDetail(String code, String description) {
        this.code = code;
        this.description = description;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public String toString() {
        return "GWFaultDetail{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

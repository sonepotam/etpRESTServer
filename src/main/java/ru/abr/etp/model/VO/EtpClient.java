package ru.abr.etp.model.VO;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;


@XmlRootElement( name="client")
@XmlAccessorType(XmlAccessType.NONE)
public class EtpClient {

    private static final long serialVersionUID = 1L;

    @XmlElement( name = "Name")
    String name;

    @XmlElement( name = "INN")
    String inn;

    @XmlElement( name = "KPP")
    String kpp;

    @XmlElement( name = "account")
    String account;

    @XmlElement( name = "saldo")
    int saldo;


    ArrayList<PackageVO> packages = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }

    public ArrayList<PackageVO> getPackages() {
        return packages;
    }

    public void setPackages(ArrayList<PackageVO> packages) {
        this.packages = packages;
    }

    public int getActualSaldo(){
        int result = getSaldo();

        int total = packages.stream()
                        .collect(Collectors.summingInt(PackageVO::getSumma));

        result = result + total;
        return result;
    }


    public EtpClient() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EtpClient etpClient = (EtpClient) o;
        return Objects.equals(name, etpClient.name) &&
                Objects.equals(inn, etpClient.inn) &&
                Objects.equals(kpp, etpClient.kpp) &&
                Objects.equals(account, etpClient.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, inn, kpp, account);
    }

    public EtpClient(String name, String inn, String kpp, String account, int saldo) {
        this.name = name;
        this.inn = inn;
        this.kpp = kpp;
        this.account = account;
        this.saldo = saldo;
    }


    @Override
    public String toString() {
        return "EtpClient{" +
                "name='" + name + '\'' +
                ", inn='" + inn + '\'' +
                ", kpp='" + kpp + '\'' +
                ", account='" + account + '\'' +
                ", saldo='" + saldo + '\'' +
                '}';
    }



}

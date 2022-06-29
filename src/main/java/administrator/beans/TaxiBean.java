package administrator.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TaxiBean {

    private int id;
    private String ipAddress;
    private int portNumber;

    public TaxiBean() {}

    public TaxiBean(int id, String ipAddress, int portNumber) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }
}

package beans;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TaxiStartInfo {

    private Position taxiStartPosition;
    private Set<TaxiBean> otherTaxis;

    public TaxiStartInfo() {}

    public TaxiStartInfo(Position taxiStartPosition, Set<TaxiBean> otherTaxis) {
        this.taxiStartPosition = taxiStartPosition;
        this.otherTaxis = otherTaxis;
    }

    public Position getTaxiStartPosition() {
        return taxiStartPosition;
    }

    public void setTaxiStartPosition(Position taxiStartPosition) {
        this.taxiStartPosition = taxiStartPosition;
    }

    public Set<TaxiBean> getOtherTaxis() {
        return otherTaxis;
    }

    public void setOtherTaxis(Set<TaxiBean> otherTaxis) {
        this.otherTaxis = otherTaxis;
    }
}

package beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaxiStartInfo {

    @XmlElement
    private Position startingPosition;
    @XmlElement
    private TaxisList taxisList;

    public TaxiStartInfo() {}

    public TaxiStartInfo(Position startingPosition, TaxisList taxisList) {
        this.startingPosition = startingPosition;
        this.taxisList = taxisList;
    }

    public Position getStartingPosition() {
        return startingPosition;
    }

    public void setStartingPosition(Position startingPosition) {
        this.startingPosition = startingPosition;
    }

    public TaxisList getTaxisList() {
        return taxisList;
    }

    public void setTaxisList(TaxisList taxisList) {
        this.taxisList = taxisList;
    }
}

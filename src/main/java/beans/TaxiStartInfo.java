package beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TaxiStartInfo {

    private Position startPosition;
    private OtherTaxisSet otherTaxis;

    public TaxiStartInfo() {}

    public TaxiStartInfo(Position startPosition, OtherTaxisSet otherTaxis) {
        this.startPosition = startPosition;
        this.otherTaxis = otherTaxis;
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Position startPosition) {
        this.startPosition = startPosition;
    }

    public OtherTaxisSet getOtherTaxis() {
        return otherTaxis;
    }

    public void setOtherTaxis(OtherTaxisSet otherTaxis) {
        this.otherTaxis = otherTaxis;
    }

    @Override
    public String toString() {
        return "Taxi starting info:" +
                "\n\tStarting position = " + startPosition +
                "\n\t" + otherTaxis;
    }
}

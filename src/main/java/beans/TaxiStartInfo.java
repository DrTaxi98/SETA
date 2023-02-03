package beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TaxiStartInfo {

    private Position startPosition;
    private OtherTaxisSet otherTaxisSet;

    public TaxiStartInfo() {}

    public TaxiStartInfo(Position startPosition, OtherTaxisSet otherTaxisSet) {
        this.startPosition = startPosition;
        this.otherTaxisSet = otherTaxisSet;
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Position startPosition) {
        this.startPosition = startPosition;
    }

    public OtherTaxisSet getOtherTaxisSet() {
        return otherTaxisSet;
    }

    public void setOtherTaxisSet(OtherTaxisSet otherTaxisSet) {
        this.otherTaxisSet = otherTaxisSet;
    }

    @Override
    public String toString() {
        return "Taxi starting info:" +
                "\n\tStarting position = " + startPosition +
                "\n\t" + otherTaxisSet;
    }
}

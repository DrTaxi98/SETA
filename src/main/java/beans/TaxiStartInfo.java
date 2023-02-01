package beans;

import utils.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TaxiStartInfo {

    private Position startPosition;
    private Set<TaxiBean> otherTaxis;

    public TaxiStartInfo() {
        otherTaxis = new LinkedHashSet<>();
    }

    public TaxiStartInfo(Position startPosition, Set<TaxiBean> otherTaxis) {
        this.startPosition = startPosition;
        this.otherTaxis = otherTaxis;
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Position startPosition) {
        this.startPosition = startPosition;
    }

    public Set<TaxiBean> getOtherTaxis() {
        return otherTaxis;
    }

    public void setOtherTaxis(Set<TaxiBean> otherTaxis) {
        this.otherTaxis = otherTaxis;
    }

    @Override
    public String toString() {
        return "Taxi starting info:" +
                "\n\tStarting position = " + startPosition +
                "\n\tOther taxis:\n\t\t" +
                StringUtils.taxisSetToString(otherTaxis, 2);
    }
}

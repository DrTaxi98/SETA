package beans;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TaxiStartInfo {

    private Position startPosition;
    private Set<TaxiBean> otherTaxis;

    public TaxiStartInfo() {}

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
}

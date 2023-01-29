package beans;

import debug.Debug;
import utils.SmartCityUtils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaxisSet {

    private Set<TaxiBean> taxis;

    private static TaxisSet instance;

    private TaxisSet() {
        taxis = new LinkedHashSet<>();
    }

    public synchronized static TaxisSet getInstance() {
        if (instance == null)
            instance = new TaxisSet();
        return instance;
    }

    public synchronized Set<TaxiBean> getTaxis() {
        Debug.sleep();
        return new LinkedHashSet<>(taxis);
    }

    public synchronized void setTaxis(Set<TaxiBean> taxis) {
        this.taxis = taxis;
    }

    public synchronized TaxiStartInfo add(TaxiBean taxi) {
        Set<TaxiBean> otherTaxis = getTaxis();

        Debug.sleep();

        if (taxis.add(taxi)) {
            Position taxiStartPosition = SmartCityUtils.getTaxiStartPosition();
            return new TaxiStartInfo(taxiStartPosition, otherTaxis);
        }
        else
            return null;
    }

    public synchronized boolean remove(int id) {
        Debug.sleep();
        return taxis.removeIf(taxi -> taxi.getId() == id);
    }

    @Override
    public String toString() {
        return "Taxis list:\n" +
                taxis.stream()
                        .map(TaxiBean::toString)
                        .collect(Collectors.joining("\n"));
    }
}

package beans;

import administrator.model.StatisticsMap;
import utils.SmartCityUtils;

import java.util.LinkedHashSet;
import java.util.Set;

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
        return new LinkedHashSet<>(taxis);
    }

    public synchronized void setTaxis(Set<TaxiBean> taxis) {
        this.taxis = taxis;
    }

    public synchronized TaxiStartInfo add(TaxiBean taxi) {
        Set<TaxiBean> otherTaxis = getTaxis();
        if (taxis.add(taxi)) {
            StatisticsMap.getInstance().addEntry(taxi.getId());
            Position startPosition = SmartCityUtils.getStartPosition();
            return new TaxiStartInfo(startPosition, otherTaxis);
        }
        else
            return null;
    }

    public synchronized boolean remove(int id) {
        return taxis.removeIf(taxi -> taxi.getId() == id);
    }
}

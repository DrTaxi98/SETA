package beans;

import debug.Debug;
import utils.SmartCityUtils;
import utils.StringUtils;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaxisSet {

    private SortedSet<TaxiBean> taxis;

    private static TaxisSet instance;

    private TaxisSet() {
        taxis = new TreeSet<>();
    }

    public synchronized static TaxisSet getInstance() {
        if (instance == null)
            instance = new TaxisSet();
        return instance;
    }

    public synchronized SortedSet<TaxiBean> getTaxis() {
        Debug.sleep();
        return new TreeSet<>(taxis);
    }

    public synchronized void setTaxis(SortedSet<TaxiBean> taxis) {
        this.taxis = taxis;
    }

    public synchronized TaxiStartInfo add(TaxiBean taxi) {
        OtherTaxisSet otherTaxis = new OtherTaxisSet(getTaxis());

        Debug.sleep();

        if (taxis.add(taxi)) {
            Position startPosition = SmartCityUtils.getTaxiStartPosition();
            return new TaxiStartInfo(startPosition, otherTaxis);
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
        return "Taxis list:\n\t" +
                StringUtils.taxisSetToString(getTaxis(), 1);
    }
}

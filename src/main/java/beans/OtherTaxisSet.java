package beans;

import debug.Debug;
import utils.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OtherTaxisSet {

    private Set<TaxiBean> otherTaxis;

    public OtherTaxisSet() {
        otherTaxis = new LinkedHashSet<>();
    }

    public OtherTaxisSet(Set<TaxiBean> otherTaxis) {
        this.otherTaxis = otherTaxis;
    }

    public synchronized Set<TaxiBean> getOtherTaxis() {
        Debug.sleep();
        return new LinkedHashSet<>(otherTaxis);
    }

    public synchronized void setOtherTaxis(Set<TaxiBean> otherTaxis) {
        this.otherTaxis = otherTaxis;
    }

    public synchronized boolean add(TaxiBean taxi) {
        Debug.sleep();
        return otherTaxis.add(taxi);
    }

    public synchronized boolean remove(int id) {
        Debug.sleep();
        return otherTaxis.removeIf(taxi -> taxi.getId() == id);
    }

    @Override
    public String toString() {
        return "Other taxis:\n\t\t" +
                StringUtils.taxisSetToString(otherTaxis, 2);
    }
}

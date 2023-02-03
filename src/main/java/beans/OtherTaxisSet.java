package beans;

import debug.Debug;
import utils.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OtherTaxisSet {

    private SortedSet<TaxiBean> otherTaxis;

    public OtherTaxisSet() {
        otherTaxis = new TreeSet<>();
    }

    public OtherTaxisSet(SortedSet<TaxiBean> otherTaxis) {
        this.otherTaxis = otherTaxis;
    }

    public synchronized SortedSet<TaxiBean> getOtherTaxis() {
        Debug.sleep();
        return new TreeSet<>(otherTaxis);
    }

    public synchronized void setOtherTaxis(SortedSet<TaxiBean> otherTaxis) {
        this.otherTaxis = otherTaxis;
    }

    public synchronized boolean add(TaxiBean taxi) {
        Debug.sleep();
        return otherTaxis.add(taxi);
    }

    public TaxiBean getNext(int id) {
        SortedSet<TaxiBean> otherTaxis = getOtherTaxis();
        for (TaxiBean otherTaxi : otherTaxis) {
            if (otherTaxi.getId() > id)
                return otherTaxi;
        }

        return otherTaxis.first();
    }

    public boolean isPresent(int id) {
        SortedSet<TaxiBean> otherTaxis = getOtherTaxis();
        return otherTaxis.stream()
                .anyMatch(taxi -> taxi.getId() == id);
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

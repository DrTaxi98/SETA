package beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaxisList {

    @XmlElement
    private List<TaxiBean> taxisList;

    private static TaxisList instance;

    private TaxisList() {
        taxisList = new ArrayList<>();
    }

    public synchronized static TaxisList getInstance() {
        if (instance == null)
            instance = new TaxisList();
        return instance;
    }

    public synchronized List<TaxiBean> getTaxisList() {
        return new ArrayList<>(taxisList);
    }

    public synchronized void setTaxisList(List<TaxiBean> taxisList) {
        this.taxisList = taxisList;
    }

    public synchronized boolean add(TaxiBean taxi) {
        return taxisList.add(taxi);
    }

    public synchronized boolean remove(int id) {
        return taxisList.removeIf(taxi -> taxi.getId() == id);
    }
}

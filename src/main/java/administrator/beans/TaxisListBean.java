package administrator.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaxisListBean {

    @XmlElement
    private List<TaxiBean> taxisList;

    private static TaxisListBean instance;

    private TaxisListBean() {
        taxisList = new ArrayList<>();
    }

    public synchronized static TaxisListBean getInstance() {
        if (instance == null)
            instance = new TaxisListBean();
        return instance;
    }

    public synchronized List<TaxiBean> getTaxisList() {
        return new ArrayList<>(taxisList);
    }

    public synchronized void setTaxisList(List<TaxiBean> taxisList) {
        this.taxisList = taxisList;
    }

    public synchronized void add(TaxiBean taxi) {
        taxisList.add(taxi);
    }

    public TaxiBean get(int id) {
        List<TaxiBean> taxisListCopy = getTaxisList();
        for (TaxiBean taxi : taxisListCopy)
            if (taxi.getId() == id)
                return taxi;
        return null;
    }
}

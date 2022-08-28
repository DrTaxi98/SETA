package beans;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class LocalStatistics {

    private int taxiId;
    private long timestamp;
    private int batteryLevel;

    private int travelledKms;
    private int accomplishedRides;
    private List<Double> pollutionAverages;

    public LocalStatistics() {}

    public LocalStatistics(int taxiId, long timestamp, int batteryLevel,
                           int travelledKms, int accomplishedRides, List<Double> pollutionAverages) {
        this.taxiId = taxiId;
        this.timestamp = timestamp;
        this.batteryLevel = batteryLevel;
        this.travelledKms = travelledKms;
        this.accomplishedRides = accomplishedRides;
        this.pollutionAverages = pollutionAverages;
    }

    public int getTaxiId() {
        return taxiId;
    }

    public void setTaxiId(int taxiId) {
        this.taxiId = taxiId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public int getTravelledKms() {
        return travelledKms;
    }

    public void setTravelledKms(int travelledKms) {
        this.travelledKms = travelledKms;
    }

    public int getAccomplishedRides() {
        return accomplishedRides;
    }

    public void setAccomplishedRides(int accomplishedRides) {
        this.accomplishedRides = accomplishedRides;
    }

    public List<Double> getPollutionAverages() {
        return pollutionAverages;
    }

    public void setPollutionAverages(List<Double> pollutionAverages) {
        this.pollutionAverages = pollutionAverages;
    }
}

package beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LocalStatistics implements Comparable<LocalStatistics> {

    private int taxiId;
    private long timestamp;
    private int batteryLevel;

    private double travelledKms;
    private int accomplishedRides;
    private List<Double> pollutionAverages;

    public LocalStatistics() {
        pollutionAverages = new ArrayList<>();
    }

    public LocalStatistics(int taxiId, long timestamp, int batteryLevel,
                           double travelledKms, int accomplishedRides, List<Double> pollutionAverages) {
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

    public double getTravelledKms() {
        return travelledKms;
    }

    public void setTravelledKms(double travelledKms) {
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

    public OptionalDouble computePollutionAverage() {
        return pollutionAverages.stream()
                .mapToDouble(Double::doubleValue)
                .average();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalStatistics that = (LocalStatistics) o;
        return taxiId == that.taxiId && timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxiId, timestamp);
    }

    @Override
    public int compareTo(LocalStatistics o) {
        int compareTimestamp = Long.compare(timestamp, o.timestamp);
        if (compareTimestamp != 0)
            return compareTimestamp;
        return Integer.compare(taxiId, o.taxiId);
    }
}

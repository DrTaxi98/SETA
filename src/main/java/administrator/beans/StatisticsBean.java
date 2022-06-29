package administrator.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StatisticsBean {

    private int travelledKms;
    private int batteryLevel;
    private double pollutionLevel;
    private int accomplishedRides;

    public StatisticsBean() {}

    public StatisticsBean(int travelledKms, int batteryLevel, double pollutionLevel, int accomplishedRides) {
        this.travelledKms = travelledKms;
        this.batteryLevel = batteryLevel;
        this.pollutionLevel = pollutionLevel;
        this.accomplishedRides = accomplishedRides;
    }

    public int getTravelledKms() {
        return travelledKms;
    }

    public void setTravelledKms(int travelledKms) {
        this.travelledKms = travelledKms;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public double getPollutionLevel() {
        return pollutionLevel;
    }

    public void setPollutionLevel(double pollutionLevel) {
        this.pollutionLevel = pollutionLevel;
    }

    public int getAccomplishedRides() {
        return accomplishedRides;
    }

    public void setAccomplishedRides(int accomplishedRides) {
        this.accomplishedRides = accomplishedRides;
    }
}

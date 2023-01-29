package beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AverageStatistics {

    private double travelledKms;
    private double batteryLevel;
    private double pollutionLevel;
    private double accomplishedRides;

    public AverageStatistics() {}

    public AverageStatistics(double travelledKms, double batteryLevel, double pollutionLevel, double accomplishedRides) {
        this.travelledKms = travelledKms;
        this.batteryLevel = batteryLevel;
        this.pollutionLevel = pollutionLevel;
        this.accomplishedRides = accomplishedRides;
    }

    public double getTravelledKms() {
        return travelledKms;
    }

    public void setTravelledKms(double travelledKms) {
        this.travelledKms = travelledKms;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public double getPollutionLevel() {
        return pollutionLevel;
    }

    public void setPollutionLevel(double pollutionLevel) {
        this.pollutionLevel = pollutionLevel;
    }

    public double getAccomplishedRides() {
        return accomplishedRides;
    }

    public void setAccomplishedRides(double accomplishedRides) {
        this.accomplishedRides = accomplishedRides;
    }

    @Override
    public String toString() {
        return "Average statistics:" +
                "\nTravelled kilometres = " + travelledKms +
                "\nBattery level = " + batteryLevel +
                "\nPollution level = " + pollutionLevel +
                "\nAccomplished rides = " + accomplishedRides;
    }
}

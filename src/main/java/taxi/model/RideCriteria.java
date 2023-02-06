package taxi.model;

import com.seta.taxi.RideServiceOuterClass.*;

import java.util.Objects;

public class RideCriteria implements Comparable<RideCriteria> {

    double distance;
    int batteryLevel;
    int taxiId;

    public RideCriteria(double distance, int batteryLevel, int taxiId) {
        this.distance = distance;
        this.batteryLevel = batteryLevel;
        this.taxiId = taxiId;
    }

    public RideCriteria(Election.Criteria criteria) {
        this(criteria.getDistance(), criteria.getBatteryLevel(), criteria.getTaxiId());
    }

    public double getDistance() {
        return distance;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public int getTaxiId() {
        return taxiId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RideCriteria that = (RideCriteria) o;
        return Double.compare(that.distance, distance) == 0 &&
                batteryLevel == that.batteryLevel &&
                taxiId == that.taxiId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(distance, batteryLevel, taxiId);
    }

    @Override
    public int compareTo(RideCriteria o) {
        int compareDistance = Double.compare(distance, o.distance);
        if (compareDistance != 0)
            return compareDistance;

        int compareBatteryLevel = Integer.compare(o.batteryLevel, batteryLevel);
        if (compareBatteryLevel != 0)
            return compareBatteryLevel;

        return Integer.compare(o.taxiId, taxiId);
    }

    @Override
    public String toString() {
        return "Ride criteria:" +
                "\n\tDistance = " + distance + " km" +
                "\n\tBatteryLevel = " + batteryLevel + '%' +
                "\n\tTaxi ID = " + taxiId;
    }
}

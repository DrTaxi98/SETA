package utils;

import com.seta.taxi.RideServiceOuterClass.*;
import taxi.model.RideRequest;
import taxi.model.RideCriteria;
import taxi.model.RideElection;
import taxi.model.Taxi;

public class RideUtils {

    public static void electionMaster(Taxi taxi, RideRequest rideRequest) {
        electionMaster(taxi, rideRequest, taxi.getRideElection(rideRequest));
    }

    public static void electionMaster(Taxi taxi, RideElection rideElection) {
        electionMaster(taxi, rideElection.getRideRequest(), rideElection);
    }

    private static void electionMaster(Taxi taxi, RideRequest rideRequest, RideElection rideElection) {
        taxi.removeRideElection(rideElection);

        int district = taxi.getDistrict();
        boolean inRideDistrict = taxi.isInDistrict(rideRequest);
        boolean available = taxi.getStatus() == Taxi.Status.ELECTING;
        printDistrictAndAvailability(taxi, district, inRideDistrict, available);

        if (inRideDistrict && available)
            taxi.accomplishRide(rideRequest);
        else if (taxi.getStatus() == Taxi.Status.ELECTING)
            taxi.setStatusAvailable();
    }

    public static String toString(Election election) {
        return new RideRequest(election.getRide()) +
                "\n" + new RideCriteria(election.getCriteria());
    }

    public static String toString(Elected elected) {
        return new RideRequest(elected.getRide()) +
                "\nTaxi ID = " + elected.getTaxiId();
    }

    public static void printDistrictAndAvailability(Taxi taxi, int district, boolean inRideDistrict, boolean available) {
        System.out.println("[Taxi " + taxi.getId() + "] District: " + district);
        System.out.println("[Taxi " + taxi.getId() + "] In ride district: " + inRideDistrict);
        System.out.println("[Taxi " + taxi.getId() + "] Available: " + available);
    }
}

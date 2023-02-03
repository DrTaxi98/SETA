package utils;

import taxi.model.Ride;
import taxi.model.RideElection;
import taxi.model.Taxi;

public class RideUtils {

    public static void electionMaster(Taxi taxi, Ride ride) {
        electionMaster(taxi, ride, taxi.getRideElection(ride));
    }

    public static void electionMaster(Taxi taxi, RideElection rideElection) {
        electionMaster(taxi, rideElection.getRide(), rideElection);
    }

    private static void electionMaster(Taxi taxi, Ride ride, RideElection rideElection) {
        taxi.removeRideElection(rideElection);
        if (taxi.isInDistrict(ride) && taxi.isAvailable())
            taxi.accomplishRide(ride);
        // else mqtt publish
    }
}

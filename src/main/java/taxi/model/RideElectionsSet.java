package taxi.model;

import beans.Position;
import debug.Debug;
import taxi.grpc.ride.RideClient;

import java.util.HashSet;
import java.util.Set;

public class RideElectionsSet {

    private final Set<RideElection> rideElections = new HashSet<>();

    private synchronized Set<RideElection> getRideElections() {
        Debug.sleep();
        return new HashSet<>(rideElections);
    }

    private synchronized boolean add(RideElection rideElection) {
        Debug.sleep();
        return rideElections.add(rideElection);
    }

    private RideElection get(Ride ride) {
        Set<RideElection> rideElections = getRideElections();
        for (RideElection rideElection : rideElections) {
            if (rideElection.getRide().equals(ride))
                return rideElection;
        }

        return null;
    }

    public synchronized RideElection getOrAdd(Ride ride, RideClient rideClient) {
        Debug.sleep();

        RideElection rideElection = get(ride);
        if (rideElection == null) {
            rideElection = new RideElection(ride, rideClient);
            add(rideElection);
        }

        return rideElection;
    }

    public RideElection getOrAdd(int rideId, RideClient rideClient) {
        Ride ride = new Ride(rideId);
        return getOrAdd(ride, rideClient);
    }

    public synchronized boolean remove(RideElection rideElection) {
        Debug.sleep();
        return rideElections.remove(rideElection);
    }
}

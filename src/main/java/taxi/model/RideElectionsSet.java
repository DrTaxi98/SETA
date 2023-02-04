package taxi.model;

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

    private synchronized void add(RideElection rideElection) {
        Debug.sleep();
        rideElections.add(rideElection);
    }

    private RideElection get(RideRequest rideRequest) {
        Set<RideElection> rideElections = getRideElections();
        for (RideElection rideElection : rideElections) {
            if (rideElection.getRideRequest().equals(rideRequest))
                return rideElection;
        }

        return null;
    }

    public synchronized RideElection getOrAdd(RideRequest rideRequest, RideClient rideClient) {
        Debug.sleep();

        RideElection rideElection = get(rideRequest);
        if (rideElection == null) {
            rideElection = new RideElection(rideRequest, rideClient);
            add(rideElection);
        }

        return rideElection;
    }

    public boolean contains(RideRequest rideRequest) {
        Set<RideElection> rideElections = getRideElections();
        return rideElections.stream()
                .anyMatch(rideElection -> rideElection.getRideRequest().equals(rideRequest));
    }

    public synchronized void remove(RideElection rideElection) {
        Debug.sleep();
        rideElections.remove(rideElection);
    }
}

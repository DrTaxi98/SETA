package seta;

import debug.Debug;
import taxi.model.RideRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RetainedRidesQueue {

    private final List<RideRequest> retainedRides = new ArrayList<>();

    private synchronized List<RideRequest> getRetainedRides() {
        Debug.sleep();
        return new ArrayList<>(retainedRides);
    }

    public synchronized boolean add(RideRequest rideRequest) {
        Debug.sleep();
        return retainedRides.add(rideRequest);
    }

    public RideRequest getFirst() {
        List<RideRequest> rides = getRetainedRides();
        if (!rides.isEmpty())
            return rides.get(0);
        return null;
    }

    public synchronized boolean remove(RideRequest rideRequest) {
        Debug.sleep();
        return retainedRides.remove(rideRequest);
    }

    @Override
    public String toString() {
        return "Retained rides:\n" +
                getRetainedRides().stream()
                        .map(RideRequest::toString)
                        .collect(Collectors.joining("\n"));
    }
}

package seta;

import debug.Debug;
import taxi.model.RideRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RetainedRidesQueue {

    private final List<RideRequest> retainedRides = new ArrayList<>();

    private synchronized List<RideRequest> getRetainedRides() {
        return new ArrayList<>(retainedRides);
    }

    public synchronized boolean add(RideRequest rideRequest) {
        Debug.sleep();
        return retainedRides.add(rideRequest);
    }

    public synchronized RideRequest removeFirst() {
        Debug.sleep();
        if (!retainedRides.isEmpty())
            return retainedRides.remove(0);
        return null;
    }

    @Override
    public String toString() {
        return "Retained rides:\n" +
                getRetainedRides().stream()
                        .map(RideRequest::toString)
                        .collect(Collectors.joining("\n"));
    }
}

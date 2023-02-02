package taxi.model;

import java.util.ArrayList;

public class RidesQueue {

    public ArrayList<RideRequest> buffer = new ArrayList<>();

    public synchronized void put(RideRequest ride) {
        buffer.add(ride);
        notify();
    }

    public synchronized RideRequest take() {
        RideRequest ride = null;

        while (buffer.size() == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ride = buffer.remove(0);
        return ride;
    }
}

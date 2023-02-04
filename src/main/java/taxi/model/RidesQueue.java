package taxi.model;

import java.util.ArrayList;

public class RidesQueue {

    public ArrayList<RideRequest> buffer = new ArrayList<>();

    private boolean shutdownRequested = false;

    public synchronized void put(RideRequest rideRequest) {
        buffer.add(rideRequest);
        notify();
    }

    public synchronized RideRequest take() {
        while (buffer.size() == 0) {
            try {
                wait();
                if (shutdownRequested)
                    return null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return buffer.remove(0);
    }

    public synchronized void shutdown() {
        shutdownRequested = true;
        notify();
    }
}

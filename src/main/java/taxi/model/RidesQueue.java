package taxi.model;

import java.util.ArrayList;

public class RidesQueue {

    public ArrayList<Ride> buffer = new ArrayList<>();

    public synchronized void put(Ride ride) {
        buffer.add(ride);
        notify();
    }

    public synchronized Ride take() {
        while (buffer.size() == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return buffer.remove(0);
    }

    public synchronized void clear() {
        buffer.clear();
    }
}

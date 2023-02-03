package taxi.model;

public class RidesConsumer extends Thread {

    private final Taxi taxi;
    private final RidesQueue ridesQueue;

    private volatile boolean shutdownRequested = false;

    public RidesConsumer(Taxi taxi, RidesQueue ridesQueue) {
        this.taxi = taxi;
        this.ridesQueue = ridesQueue;
    }

    public void run() {
        while (!shutdownRequested) {
            consume(ridesQueue.take());
        }
    }

    public void consume(Ride ride) {
        taxi.getRideElection(ride).startElection();
    }

    public void shutdown() {
        shutdownRequested = true;
    }
}

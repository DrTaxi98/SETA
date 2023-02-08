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
            RideRequest rideRequest = ridesQueue.take();
            if (rideRequest != null)
                consume(rideRequest);
        }
    }

    public void consume(RideRequest rideRequest) {
        if (taxi.getStatus() == Taxi.Status.AVAILABLE) {
            taxi.getRideElection(rideRequest).startElection();
        }
    }

    public void shutdown() {
        shutdownRequested = true;
        ridesQueue.shutdown();
    }
}

package taxi.model;

import com.seta.taxi.RideServiceOuterClass.*;
import taxi.grpc.ride.RideClient;

import java.util.Objects;

public class RideElection {

    private final RideRequest rideRequest;
    private final RideClient rideClient;
    private volatile boolean participant = false;

    public RideElection(RideRequest rideRequest, RideClient rideClient) {
        this.rideRequest = rideRequest;
        this.rideClient = rideClient;
    }

    public RideRequest getRideRequest() {
        return rideRequest;
    }

    public void startElection() {
        System.out.println("Starting Ride " + rideRequest.getId() + " election...");
        participant = true;
        rideClient.sendElection(rideRequest);
    }

    public void sendElection() {
        if (!participant) {
            participant = true;
            rideClient.sendElection(rideRequest);
        }
    }

    public void forwardElection(Election election) {
        participant = true;
        rideClient.forwardElection(election);
    }

    public void sendElected() {
        //participant = false;
        rideClient.sendElected(rideRequest);
    }

    public void forwardElected(Elected elected) {
        //participant = false;
        rideClient.forwardElected(elected);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RideElection that = (RideElection) o;
        return Objects.equals(rideRequest, that.rideRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rideRequest);
    }
}

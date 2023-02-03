package taxi.model;

import com.seta.taxi.RideServiceOuterClass.*;
import taxi.grpc.ride.RideClient;

import java.util.Objects;

public class RideElection {

    private final Ride ride;
    private final RideClient rideClient;
    private boolean participant = false;

    public RideElection(Ride ride, RideClient rideClient) {
        this.ride = ride;
        this.rideClient = rideClient;
    }

    public Ride getRide() {
        return ride;
    }

    public void startElection() {
        participant = true;
        rideClient.sendElection(ride);
    }

    public void sendElection() {
        if (!participant) {
            participant = true;
            rideClient.sendElection(ride);
        }
    }

    public void forwardElection(Election election) {
        participant = true;
        rideClient.forwardElection(election);
    }

    public void sendElected() {
        participant = false;
        rideClient.sendElected(ride);
    }

    public void forwardElected(Elected elected) {
        participant = false;
        rideClient.forwardElected(elected);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RideElection that = (RideElection) o;
        return Objects.equals(ride, that.ride);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ride);
    }
}

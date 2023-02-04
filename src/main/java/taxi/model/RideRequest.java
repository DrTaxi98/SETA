package taxi.model;

import beans.Position;
import com.seta.taxi.RideServiceOuterClass.*;

import java.util.Objects;

public class RideRequest {

    private final int id;
    private final Position startingPosition;
    private final Position destinationPosition;

    public RideRequest(int id, Position startingPosition, Position destinationPosition) {
        this.id = id;
        this.startingPosition = startingPosition;
        this.destinationPosition = destinationPosition;
    }

    public RideRequest(Ride ride) {
        this(ride.getId(),
                new Position(ride.getStartingPosition()),
                new Position(ride.getDestinationPosition()));
    }

    public int getId() {
        return id;
    }

    public Position getStartingPosition() {
        return startingPosition;
    }

    public Position getDestinationPosition() {
        return destinationPosition;
    }

    public double getDistance() {
        return startingPosition.distanceFrom(destinationPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RideRequest rideRequest = (RideRequest) o;
        return id == rideRequest.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ride:" +
                "\n\tID = " + id +
                "\n\tStarting position\t = " + startingPosition +
                "\n\tDestination position = " + destinationPosition;
    }
}

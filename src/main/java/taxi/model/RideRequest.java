package taxi.model;

import beans.Position;

public class RideRequest {

    private static int ID = 0;

    private final int id;
    private final Position startingPosition;
    private final Position destinationPosition;

    public RideRequest(Position startingPosition, Position destinationPosition) {
        id = ID++;
        this.startingPosition = startingPosition;
        this.destinationPosition = destinationPosition;
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

    @Override
    public String toString() {
        return "Ride request:" +
                "\n\tID = " + id +
                "\n\tStarting position\t = " + startingPosition +
                "\n\tDestination position = " + destinationPosition;
    }
}

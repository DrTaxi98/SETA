package utils;

import beans.Position;
import taxi.model.RideRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SmartCityUtils {

    private static final int SIZE = 10;
    public static final int DISTRICTS = 4;
    private static final int DISTRICT_SIZE = SIZE / 2;
    private static final List<Position> rechargingStations =
            Arrays.asList(
                    new Position(),
                    new Position(0, SIZE - 1),
                    new Position(SIZE - 1, SIZE - 1),
                    new Position(SIZE - 1, 0)
            );

    private static boolean isCoordinateInvalid(int coordinate) {
        return coordinate < 0 || coordinate >= SIZE;
    }

    private static boolean isPositionInvalid(int x, int y) {
        return isCoordinateInvalid(x) || isCoordinateInvalid(y);
    }

    public static void checkPosition(int x, int y) {
        if (isPositionInvalid(x, y))
            throw new IllegalArgumentException("Position is outside of the smart city size");
    }

    private static final Random random = new Random();

    private static int randomDistrict() {
        return random.nextInt(DISTRICTS) + 1;
    }

    public static Position randomPosition() {
        int x = random.nextInt(SIZE);
        int y = random.nextInt(SIZE);
        return new Position(x, y);
    }

    public static RideRequest randomRideRequest() {
        Position startingPosition = randomPosition();
        Position destinationPosition;

        do {
            destinationPosition = randomPosition();
        } while (startingPosition.equals(destinationPosition));

        return new RideRequest(startingPosition, destinationPosition);
    }

    public static int getDistrict(int x, int y) {
        checkPosition(x, y);

        if (x < DISTRICT_SIZE)
            if (y < DISTRICT_SIZE)
                return 1;
            else
                return 2;
        else if (y < DISTRICT_SIZE)
            return 4;
        else
            return 3;
    }

    public static Position getRechargingStation(int district) {
        return rechargingStations.get(district - 1);
    }

    public static Position getTaxiStartPosition() {
        return getRechargingStation(randomDistrict());
    }
}

package utils;

import beans.Position;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SmartCityUtils {

    private static final int SIZE = 10;
    private static final int DISTRICTS = 4;
    private static final int DISTRICT_SIZE = SIZE / 2;
    private static final List<Position> rechargingStations =
            Arrays.asList(
                    new Position(),
                    new Position(0, SIZE - 1),
                    new Position(SIZE - 1, SIZE - 1),
                    new Position(SIZE - 1, 0)
            );

    private static final Random random = new Random();

    private static int randomDistrict() {
        return random.nextInt(DISTRICTS) + 1;
    }

    public static int getDistrict(Position position) {
        int x = position.getX();
        int y = position.getY();

        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE)
            throw new RuntimeException();

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

    public static Position getStartPosition() {
        return getRechargingStation(randomDistrict());
    }
}

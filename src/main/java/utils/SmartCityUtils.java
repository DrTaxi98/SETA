package utils;

import beans.Position;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SmartCityUtils {

    private static final int SIZE = 10;
    private static final int DISCRICTS = 4;
    private static final int DISTRICT_SIZE = SIZE / 2;
    private static final List<Position> rechargingStations =
            Arrays.asList(
                    new Position(),
                    new Position(0, SIZE - 1),
                    new Position(SIZE - 1, SIZE - 1),
                    new Position(SIZE - 1, 0)
            );

    private static final Random random = new Random();

    public static int getDistrict(Position position) {
        int x = position.getX();
        int y = position.getY();

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

    public static int randomDistrict() {
        return random.nextInt(DISCRICTS) + 1;
    }

    public static Position getRechargingStation(int district) {
        return rechargingStations.get(district - 1);
    }
}

package smartcity;

public class SmartCity {

    private final Cell[][] grid;

    private static SmartCity instance;

    private SmartCity() {
        grid = new Cell[10][];
        for (Cell[] row : grid)
            row = new Cell[10];
    }

    public synchronized static SmartCity getInstance() {
        if (instance == null)
            instance = new SmartCity();
        return instance;
    }
}

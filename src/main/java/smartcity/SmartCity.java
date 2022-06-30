package smartcity;

import administrator.beans.TaxisListBean;

public class SmartCity {

    public static final int SIZE = 10;

    private final Cell[][] grid = new Cell[SIZE][];
    private final TaxisListBean taxis = TaxisListBean.getInstance();

    private static SmartCity instance;

    private SmartCity() {
        for (Cell[] row : grid)
            row = new Cell[SIZE];
    }

    public synchronized static SmartCity getInstance() {
        if (instance == null)
            instance = new SmartCity();
        return instance;
    }
}

package administrator.model;

import beans.LocalStatistics;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class LocalStatisticsMap {

    private final Map<Integer, SortedSet<LocalStatistics>> localStatisticsMap = new HashMap<>();

    private static LocalStatisticsMap instance;

    private LocalStatisticsMap() {}

    public synchronized static LocalStatisticsMap getInstance() {
        if (instance == null)
            instance = new LocalStatisticsMap();
        return instance;
    }

    private synchronized Map<Integer, SortedSet<LocalStatistics>> getLocalStatisticsMap() {
        return new HashMap<>(localStatisticsMap);
    }

    public synchronized boolean add(LocalStatistics stats) {
        int taxiId = stats.getTaxiId();
        localStatisticsMap.putIfAbsent(taxiId, new TreeSet<>());
        return localStatisticsMap.get(taxiId).add(stats);
    }

    public SortedSet<LocalStatistics> get(int id) {
        Map<Integer, SortedSet<LocalStatistics>> statsMapCopy = getLocalStatisticsMap();
        return statsMapCopy.get(id);
    }
}

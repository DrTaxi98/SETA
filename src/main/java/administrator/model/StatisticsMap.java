package administrator.model;

import beans.AverageStatistics;
import beans.LocalStatistics;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsMap {

    private final Map<Integer, SortedSet<LocalStatistics>> statisticsMap = new HashMap<>();

    private static StatisticsMap instance;

    private StatisticsMap() {}

    public synchronized static StatisticsMap getInstance() {
        if (instance == null)
            instance = new StatisticsMap();
        return instance;
    }

    private synchronized Map<Integer, SortedSet<LocalStatistics>> getStatisticsMap() {
        return statisticsMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new TreeSet<>(entry.getValue())));
    }

    private AverageStatistics computeAverage(Stream<LocalStatistics> statsStream) {
        OptionalDouble travelledKms = statsStream
                .mapToDouble(LocalStatistics::getTravelledKms)
                .average();
        if (!travelledKms.isPresent())
            return null;

        OptionalDouble batteryLevel = statsStream
                .mapToInt(LocalStatistics::getBatteryLevel)
                .average();
        if (!batteryLevel.isPresent())
            return null;

        OptionalDouble pollutionLevel = statsStream
                .map(LocalStatistics::computePollutionAverage)
                .filter(OptionalDouble::isPresent)
                .mapToDouble(OptionalDouble::getAsDouble)
                .average();
        if (!pollutionLevel.isPresent())
            return null;

        OptionalDouble accomplishedRides = statsStream
                .mapToInt(LocalStatistics::getAccomplishedRides)
                .average();
        if (!accomplishedRides.isPresent())
            return null;

        return new AverageStatistics(travelledKms.getAsDouble(), batteryLevel.getAsDouble(),
                pollutionLevel.getAsDouble(), accomplishedRides.getAsDouble());
    }

    public synchronized void addEntry(int id) {
        statisticsMap.putIfAbsent(id, new TreeSet<>());
    }

    public synchronized Boolean add(LocalStatistics stats) {
        SortedSet<LocalStatistics> statsSet = statisticsMap.get(stats.getTaxiId());
        if (statsSet != null)
            return statsSet.add(stats);
        else
            return null;
    }

    public AverageStatistics getTaxiAverage(int id, int n) {
        SortedSet<LocalStatistics> statsSet = getStatisticsMap().get(id);
        if (statsSet == null)
            return null;

        n = Math.min(n, statsSet.size());

        Stream<LocalStatistics> statsStream = statsSet.stream()
                .skip(statsSet.size() - n);
        return computeAverage(statsStream);
    }

    public AverageStatistics getAverage(long t1, long t2) {
        Stream<LocalStatistics> statsStream = getStatisticsMap().values().stream()
                .flatMap(Collection::stream)
                .filter(stats -> stats.getTimestamp() >= t1 && stats.getTimestamp() <= t2);
        return computeAverage(statsStream);
    }
}

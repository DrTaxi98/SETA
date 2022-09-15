package administrator.model;

import beans.AverageStatistics;
import beans.LocalStatistics;

import java.util.List;
import java.util.OptionalDouble;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class StatisticsSet {

    private final SortedSet<LocalStatistics> statisticsSet = new TreeSet<>();

    private static StatisticsSet instance;

    private StatisticsSet() {}

    public synchronized static StatisticsSet getInstance() {
        if (instance == null)
            instance = new StatisticsSet();
        return instance;
    }

    private synchronized SortedSet<LocalStatistics> getStatisticsSet() {
        return new TreeSet<>(statisticsSet);
    }

    private AverageStatistics computeAverage(List<LocalStatistics> statsList) {
        OptionalDouble travelledKms = statsList.stream()
                .mapToDouble(LocalStatistics::getTravelledKms)
                .average();
        if (!travelledKms.isPresent())
            return null;

        OptionalDouble batteryLevel = statsList.stream()
                .mapToInt(LocalStatistics::getBatteryLevel)
                .average();
        if (!batteryLevel.isPresent())
            return null;

        OptionalDouble pollutionLevel = statsList.stream()
                .map(LocalStatistics::computePollutionAverage)
                .filter(OptionalDouble::isPresent)
                .mapToDouble(OptionalDouble::getAsDouble)
                .average();
        if (!pollutionLevel.isPresent())
            return null;

        OptionalDouble accomplishedRides = statsList.stream()
                .mapToInt(LocalStatistics::getAccomplishedRides)
                .average();
        if (!accomplishedRides.isPresent())
            return null;

        return new AverageStatistics(travelledKms.getAsDouble(), batteryLevel.getAsDouble(),
                pollutionLevel.getAsDouble(), accomplishedRides.getAsDouble());
    }

    public synchronized boolean add(LocalStatistics stats) {
        return statisticsSet.add(stats);
    }

    public AverageStatistics getTaxiAverage(int id, int n) {
        List<LocalStatistics> taxiStatsList = getStatisticsSet().stream()
                .filter(stats -> stats.getTaxiId() == id)
                .collect(Collectors.toList());

        int fromIndex = Math.max(taxiStatsList.size() - n, 0);
        return computeAverage(taxiStatsList.subList(fromIndex, taxiStatsList.size()));
    }

    public AverageStatistics getAverage(long t1, long t2) {
        List<LocalStatistics> statsList = getStatisticsSet().stream()
                .filter(stats -> stats.getTimestamp() >= t1 && stats.getTimestamp() <= t2)
                .collect(Collectors.toList());
        return computeAverage(statsList);
    }
}

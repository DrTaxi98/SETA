package taxi.statistics;

import beans.LocalStatistics;
import taxi.model.Taxi;

import java.util.ArrayList;
import java.util.List;

public class StatisticsComputer extends Thread {

    private static final int COMPUTING_TIME = 15000;

    private final Taxi taxi;

    private double travelledKms;
    private int accomplishedRides;
    private List<Double> pollutionAverages = new ArrayList<>();

    private volatile boolean shutdownRequested = false;

    public StatisticsComputer(Taxi taxi) {
        this.taxi = taxi;
    }

    private synchronized LocalStatistics getStats() {
        LocalStatistics stats = new LocalStatistics(taxi.getId(), currentTimestamp(), taxi.getBatteryLevel(),
                travelledKms, accomplishedRides, pollutionAverages);
        travelledKms = 0;
        accomplishedRides = 0;
        pollutionAverages = new ArrayList<>();
        return stats;
    }

    public synchronized void addTravelledKms(double kms) {
        travelledKms += kms;
    }

    public synchronized void incrementAccomplishedRides() {
        accomplishedRides++;
    }

    public synchronized void addPollutionAverage(double average) {
        pollutionAverages.add(average);
    }

    @Override
    public void run() {
        while (!shutdownRequested) {
            try {
                Thread.sleep(COMPUTING_TIME);
                taxi.sendStatistics(computeStatistics());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private LocalStatistics computeStatistics() {
        LocalStatistics stats = getStats();
        System.out.println("Computed " + stats);
        return stats;
    }

    private long currentTimestamp() {
        return System.currentTimeMillis();
    }

    public void shutdown() {
        shutdownRequested = true;
    }
}

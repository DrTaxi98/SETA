package taxi.statistics;

import taxi.simulators.Measurement;
import taxi.simulators.SlidingWindow;

import java.util.List;
import java.util.OptionalDouble;

public class SensorReader extends Thread {

    private final StatisticsComputer statisticsComputer;
    private final SlidingWindow slidingWindow;

    private volatile boolean shutdownRequested = false;

    public SensorReader(StatisticsComputer statisticsComputer, SlidingWindow slidingWindow) {
        this.statisticsComputer = statisticsComputer;
        this.slidingWindow = slidingWindow;
    }

    public void run() {
        while (!shutdownRequested) {
            consume(slidingWindow.readAllAndClean());
        }
    }

    public void consume(List<Measurement> measurements) {
        OptionalDouble average = measurements.stream()
                .mapToDouble(Measurement::getValue)
                .average();
        if (average.isPresent())
            statisticsComputer.addPollutionAverage(average.getAsDouble());
    }

    public void shutdown() {
        shutdownRequested = true;
        slidingWindow.shutdown();
    }
}

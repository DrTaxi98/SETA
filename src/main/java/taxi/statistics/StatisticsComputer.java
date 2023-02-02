package taxi.statistics;

public class StatisticsComputer extends Thread {

    private volatile boolean shutdownRequested = false;

    @Override
    public void run() {
        while (!shutdownRequested) {

        }
    }

    public void shutdown() {
        shutdownRequested = true;
    }
}

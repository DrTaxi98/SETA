package taxi.simulators;

import debug.Debug;

import java.util.ArrayList;
import java.util.List;

public class SlidingWindow implements Buffer {

    private static final int W = 8;
    private static final double O = 0.5;
    private static final int DISCARD = (int) (O * W);

    private final List<Measurement> measurements = new ArrayList<>(W);

    private boolean shutdownRequested = false;

    @Override
    public synchronized void addMeasurement(Measurement m) {
        //System.out.println("[Sensor] Adding measurement: " + m);
        Debug.sleep();
        measurements.add(m);
        //System.out.println("[Sensor] Measurement added.");
        notify();
    }

    @Override
    public synchronized List<Measurement> readAllAndClean() {
        while (measurements.size() < W && !shutdownRequested) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<Measurement> readMeasurements = new ArrayList<>(measurements);
        //System.out.println("[Sensor] Read measurements: " + readMeasurements);

        Debug.sleep();

        measurements.subList(0, Math.min(DISCARD, measurements.size())).clear();
        //System.out.println("[Sensor] Discarded old measurements. Remaining: " + measurements);

        return readMeasurements;
    }

    public synchronized void shutdown() {
        shutdownRequested = true;
        notifyAll();
    }
}

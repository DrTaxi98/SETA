package taxi.grpc;

import beans.OtherTaxisSet;
import beans.TaxiBean;
import taxi.model.RideRequest;
import taxi.model.Taxi;

import java.util.ArrayList;
import java.util.Set;

public class TaxiGrpcClient {

    private final Taxi taxi;

    public TaxiGrpcClient(Taxi taxi) {
        this.taxi = taxi;
    }

    public void present(OtherTaxisSet otherTaxisSet) {
        Set<TaxiBean> otherTaxis = otherTaxisSet.getOtherTaxis();
        ArrayList<Thread> threads = new ArrayList<>();
        for (TaxiBean otherTaxi : otherTaxis) {
            PresentThread presentThread = new PresentThread(taxi, otherTaxi);
            threads.add(presentThread);
            presentThread.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyQuit(OtherTaxisSet otherTaxisSet) {
        Set<TaxiBean> otherTaxis = otherTaxisSet.getOtherTaxis();
        ArrayList<Thread> threads = new ArrayList<>();
        for (TaxiBean otherTaxi : otherTaxis) {
            NotifyQuitThread notifyQuitThread = new NotifyQuitThread(taxi, otherTaxi);
            threads.add(notifyQuitThread);
            notifyQuitThread.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void startElection(RideRequest ride) {

    }
}

package taxi.grpc.presentation;

import beans.OtherTaxisSet;
import beans.TaxiBean;
import com.seta.taxi.PresentationServiceOuterClass.*;
import taxi.model.Taxi;

import java.util.ArrayList;
import java.util.SortedSet;

public class PresentationClient {

    private final Taxi taxi;

    public PresentationClient(Taxi taxi) {
        this.taxi = taxi;
    }

    public void present(OtherTaxisSet otherTaxisSet) {
        TaxiPresentation request = TaxiPresentation.newBuilder()
                .setId(taxi.getId())
                .setIpAddress(taxi.getIpAddress())
                .setPortNumber(taxi.getPortNumber())
                .setPosition(TaxiPresentation.Position.newBuilder()
                        .setX(taxi.getPosition().getX())
                        .setY(taxi.getPosition().getY())
                        .build())
                .build();

        SortedSet<TaxiBean> otherTaxis = otherTaxisSet.getOtherTaxis();
        ArrayList<Thread> threads = new ArrayList<>();
        for (TaxiBean otherTaxi : otherTaxis) {
            PresentThread presentThread = new PresentThread(taxi, otherTaxi, request);
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
        TaxiId request = TaxiId.newBuilder()
                .setId(taxi.getId())
                .build();

        SortedSet<TaxiBean> otherTaxis = otherTaxisSet.getOtherTaxis();
        ArrayList<Thread> threads = new ArrayList<>();
        for (TaxiBean otherTaxi : otherTaxis) {
            NotifyQuitThread notifyQuitThread = new NotifyQuitThread(taxi, otherTaxi, request);
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
}

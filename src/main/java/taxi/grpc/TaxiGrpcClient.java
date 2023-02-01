package taxi.grpc;

import beans.OtherTaxisSet;
import beans.TaxiBean;
import com.seta.taxi.PresentationServiceGrpc.*;
import com.seta.taxi.PresentationServiceGrpc;
import com.seta.taxi.PresentationServiceOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import taxi.model.Taxi;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TaxiGrpcClient {

    private final Taxi taxi;

    public TaxiGrpcClient(Taxi taxi) {
        this.taxi = taxi;
    }

    public void present(OtherTaxisSet otherTaxisSet) {
        Set<TaxiBean> otherTaxis = otherTaxisSet.getOtherTaxis();
        ArrayList<Thread> presentationThreads = new ArrayList<>();
        for (TaxiBean otherTaxi : otherTaxis) {
            PresentationThread presentationThread = new PresentationThread(taxi, otherTaxi);
            presentationThreads.add(presentationThread);
            presentationThread.start();
        }

        for (Thread t : presentationThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

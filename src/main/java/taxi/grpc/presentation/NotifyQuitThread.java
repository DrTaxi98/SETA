package taxi.grpc.presentation;

import beans.TaxiBean;
import com.seta.taxi.PresentationServiceGrpc;
import com.seta.taxi.PresentationServiceGrpc.*;
import com.seta.taxi.PresentationServiceOuterClass.*;
import debug.Debug;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import taxi.model.Taxi;
import utils.GrpcUtils;

import java.util.concurrent.TimeUnit;

public class NotifyQuitThread extends Thread {

    private final Taxi taxi;
    private final TaxiBean otherTaxi;

    public NotifyQuitThread(Taxi taxi, TaxiBean otherTaxi) {
        this.taxi = taxi;
        this.otherTaxi = otherTaxi;
    }

    @Override
    public void run() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(otherTaxi.computeSocketAddress())
                .usePlaintext()
                .build();
        PresentationServiceStub stub = PresentationServiceGrpc.newStub(channel);

        TaxiId request = TaxiId.newBuilder()
                .setId(taxi.getId())
                .build();

        System.out.println("[Taxi " + taxi.getId() + "] Notifying Taxi " + otherTaxi.getId());

        Debug.sleep();

        stub.notifyQuit(request, new StreamObserver<TaxiResponse>() {

            public void onNext(TaxiResponse taxiResponse) {
                int id = taxiResponse.getId();
                if (taxiResponse.getOk())
                    System.out.println("[Taxi " + taxi.getId() + "] Taxi " + id + " accepted the notification.");
                else
                    System.out.println("[Taxi " + taxi.getId() + "] Taxi " + id + " did not accept the notification.");
            }

            public void onError(Throwable throwable) {
                System.out.println("Error! " + throwable.getMessage());
                GrpcUtils.handleInactiveTaxi(taxi, otherTaxi.getId());
            }

            public void onCompleted() {
                channel.shutdownNow();
            }
        });

        try {
            channel.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

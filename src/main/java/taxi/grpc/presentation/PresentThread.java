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

public class PresentThread extends Thread {

    private final Taxi taxi;
    private final TaxiBean otherTaxi;
    private final TaxiPresentation request;

    public PresentThread(Taxi taxi, TaxiBean otherTaxi, TaxiPresentation request) {
        this.taxi = taxi;
        this.otherTaxi = otherTaxi;
        this.request = request;
    }

    @Override
    public void run() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(otherTaxi.computeSocketAddress())
                .usePlaintext()
                .build();
        PresentationServiceStub stub = PresentationServiceGrpc.newStub(channel);

        System.out.println("[Taxi " + taxi.getId() + "] Presenting to Taxi " + otherTaxi.getId());

        Debug.sleep();

        stub.present(request, new StreamObserver<TaxiResponse>() {

            public void onNext(TaxiResponse taxiResponse) {
                int id = taxiResponse.getId();
                if (taxiResponse.getOk())
                    System.out.println("[Taxi " + taxi.getId() + "] Taxi " + id + " accepted the presentation.");
                else
                    System.out.println("[Taxi " + taxi.getId() + "] Taxi " + id + " did not accept the presentation.");
            }

            public void onError(Throwable throwable) {
                System.out.println("Error! " + throwable.getMessage());
                GrpcUtils.handleInactiveTaxi(taxi, otherTaxi.getId());
                channel.shutdownNow();
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

package taxi.grpc;

import beans.TaxiBean;
import com.seta.taxi.PresentationServiceGrpc;
import com.seta.taxi.PresentationServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import taxi.model.Taxi;

import java.util.concurrent.TimeUnit;

public class PresentationThread extends Thread {

    private final Taxi taxi;
    private final TaxiBean otherTaxi;

    public PresentationThread(Taxi taxi, TaxiBean otherTaxi) {
        this.taxi = taxi;
        this.otherTaxi = otherTaxi;
    }

    @Override
    public void run() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(otherTaxi.computeSocketAddress())
                .usePlaintext()
                .build();
        PresentationServiceGrpc.PresentationServiceStub stub = PresentationServiceGrpc.newStub(channel);

        PresentationServiceOuterClass.TaxiPresentation request = PresentationServiceOuterClass.TaxiPresentation.newBuilder()
                .setId(taxi.getId())
                .setIpAddress(taxi.getIpAddress())
                .setPortNumber(taxi.getPortNumber())
                .setPosition(PresentationServiceOuterClass.TaxiPresentation.Position.newBuilder()
                        .setX(taxi.getPosition().getX())
                        .setY(taxi.getPosition().getY())
                        .build())
                .build();

        stub.present(request, new StreamObserver<PresentationServiceOuterClass.OkResponse>() {

            public void onNext(PresentationServiceOuterClass.OkResponse response) {
                if (response.getOk())
                    System.out.println("A taxi accepted the presentation.");
                else
                    System.out.println("A taxi did not accept the presentation.");
            }

            public void onError(Throwable throwable) {
                System.out.println("Error! " + throwable.getMessage());
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

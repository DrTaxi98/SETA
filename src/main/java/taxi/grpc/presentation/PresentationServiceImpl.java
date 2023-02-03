package taxi.grpc.presentation;

import beans.Position;
import beans.TaxiBean;
import com.seta.taxi.PresentationServiceGrpc.*;
import com.seta.taxi.PresentationServiceOuterClass.*;
import io.grpc.stub.StreamObserver;
import taxi.model.Taxi;

public class PresentationServiceImpl extends PresentationServiceImplBase {

    private final Taxi taxi;

    public PresentationServiceImpl(Taxi taxi) {
        this.taxi = taxi;
    }

    @Override
    public void present(TaxiPresentation request, StreamObserver<TaxiResponse> responseObserver) {
        int id = request.getId();
        String ipAddress = request.getIpAddress();
        int portNumber = request.getPortNumber();
        TaxiBean otherTaxi = new TaxiBean(id, ipAddress, portNumber);
        System.out.println("A taxi presented itself: " + otherTaxi);

        int x = request.getPosition().getX();
        int y = request.getPosition().getY();
        Position position = new Position(x, y);
        System.out.println("\t" + position);

        boolean result = taxi.addOtherTaxi(otherTaxi);
        if (result)
            System.out.println("Taxi " + id + " added.");
        else
            System.out.println("Taxi " + id + " is already present.");

        System.out.println(taxi.getOtherTaxisSet());

        TaxiResponse response = TaxiResponse.newBuilder()
                .setId(taxi.getId())
                .setOk(result)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void notifyQuit(TaxiId request, StreamObserver<TaxiResponse> responseObserver) {
        int id = request.getId();
        System.out.println("Taxi " + id + " is leaving the system.");

        boolean result = taxi.removeOtherTaxi(id);
        if (result)
            System.out.println("Taxi " + id + " removed.");
        else
            System.out.println("Taxi " + id + " does not exist.");

        System.out.println(taxi.getOtherTaxisSet());

        TaxiResponse response = TaxiResponse.newBuilder()
                .setId(taxi.getId())
                .setOk(result)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

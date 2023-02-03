package taxi.grpc.ride;

import com.seta.taxi.RideServiceGrpc.*;
import com.seta.taxi.RideServiceOuterClass.*;
import io.grpc.stub.StreamObserver;
import taxi.model.Ride;
import taxi.model.RideCriteria;
import taxi.model.RideElection;
import taxi.model.Taxi;
import utils.RideUtils;

public class RideServiceImpl extends RideServiceImplBase {
    private final Taxi taxi;

    public RideServiceImpl(Taxi taxi) {
        this.taxi = taxi;
    }

    @Override
    public void elect(Election request, StreamObserver<ElectionResponse> responseObserver) {
        Ride ride = new Ride(request.getRide());

        double otherDistance = request.getCriteria().getDistance();
        int otherBatteryLevel = request.getCriteria().getBatteryLevel();
        int otherTaxiId = request.getCriteria().getTaxiId();

        RideCriteria otherRideCriteria = new RideCriteria(otherDistance, otherBatteryLevel, otherTaxiId);

        System.out.println("[Taxi " + taxi.getId() + "] Received ELECTION message:" +
                '\n' + ride +
                '\n' + otherRideCriteria);

        ElectionResponse response = ElectionResponse.newBuilder()
                .setTaxiId(taxi.getId())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        RideCriteria rideCriteria = taxi.getRideCriteria(ride);
        System.out.println("[Taxi " + taxi.getId() + "] " + rideCriteria);
        int compareCriteria = rideCriteria.compareTo(otherRideCriteria);

        RideElection rideElection = taxi.getRideElection(ride);

        if (!taxi.isOtherTaxiPresent(otherTaxiId) && otherTaxiId != taxi.getId()) {
            System.out.println("[Taxi " + taxi.getId() + "] Taxi " + otherTaxiId + " is no longer present.");
            rideElection.startElection();
        }

        if (rideCriteria.getTaxiId() == otherRideCriteria.getTaxiId()) {
            rideElection.sendElected();
            taxi.removeRideElection(rideElection);
            if (taxi.isInDistrict(ride) && taxi.isAvailable())
                taxi.accomplishRide(ride);
            // else mqtt publish
        }
        else if (!taxi.isInDistrict(ride) || !taxi.isAvailable() || compareCriteria > 0)
            rideElection.forwardElection(request);
        else
            rideElection.sendElection();
    }

    @Override
    public void stopElection(Elected request, StreamObserver<ElectionResponse> responseObserver) {
        int rideId = request.getRideId();
        int taxiId = request.getTaxiId();

        System.out.println("[Taxi " + taxi.getId() + "] Received ELECTED message:" +
                "\nRide ID = " + rideId +
                "\nTaxi ID = " + taxiId);

        ElectionResponse response = ElectionResponse.newBuilder()
                .setTaxiId(taxi.getId())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        RideElection rideElection = taxi.getRideElection(rideId);
        if (taxiId != taxi.getId()) {
            rideElection.forwardElected(request);
            taxi.removeRideElection(rideElection);
        }
        else
            RideUtils.electionMaster(taxi, rideElection);
    }
}

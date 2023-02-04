package taxi.grpc.ride;

import com.seta.taxi.RideServiceGrpc.*;
import com.seta.taxi.RideServiceOuterClass.*;
import debug.Debug;
import io.grpc.stub.StreamObserver;
import taxi.model.RideRequest;
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
        System.out.println("[Taxi " + taxi.getId() + "] Received ELECTION message:" +
                '\n' + RideUtils.toString(request));

        RideRequest rideRequest = new RideRequest(request.getRide());
        RideCriteria otherRideCriteria = new RideCriteria(request.getCriteria());

        Debug.sleep();

        ElectionResponse response = ElectionResponse.newBuilder()
                .setTaxiId(taxi.getId())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        int district = taxi.getDistrict();
        boolean inRideDistrict = taxi.isInDistrict(rideRequest);
        boolean available = taxi.isAvailable();
        RideUtils.printDistrictAndAvailability(taxi, district, inRideDistrict, available);

        RideCriteria rideCriteria = taxi.getRideCriteria(rideRequest);
        System.out.println("[Taxi " + taxi.getId() + "] " + rideCriteria);

        RideElection rideElection = taxi.getRideElection(rideRequest);

        if (taxi.isOtherTaxiAbsent(otherRideCriteria.getTaxiId()) &&
                rideCriteria.getTaxiId() != otherRideCriteria.getTaxiId()) {
            System.out.println("[Taxi " + taxi.getId() + "]" +
                    "Taxi " + otherRideCriteria.getTaxiId() + " is no longer present.");
            rideElection.startElection();
        }
        else if (rideCriteria.getTaxiId() == otherRideCriteria.getTaxiId()) {
            System.out.println("[Taxi " + taxi.getId() + "] Elected for Ride " + rideRequest.getId());
            rideElection.sendElected();
        }
        else {
            int compareCriteria = rideCriteria.compareTo(otherRideCriteria);
            if (!inRideDistrict || !available || compareCriteria > 0)
                rideElection.forwardElection(request);
            else
                rideElection.sendElection();
        }
    }

    @Override
    public void stopElection(Elected request, StreamObserver<ElectionResponse> responseObserver) {
        System.out.println("[Taxi " + taxi.getId() + "] Received ELECTED message:" +
                '\n' + RideUtils.toString(request));

        RideRequest rideRequest = new RideRequest(request.getRide());
        int electedTaxiId = request.getTaxiId();

        Debug.sleep();

        ElectionResponse response = ElectionResponse.newBuilder()
                .setTaxiId(taxi.getId())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        if (taxi.isOtherTaxiAbsent(electedTaxiId) &&
                electedTaxiId != taxi.getId() &&
                !taxi.isPresentRideElection(rideRequest)) {
            System.out.println("[Taxi " + taxi.getId() + "]" + "Taxi " + electedTaxiId + " is no longer present.");
            taxi.publishRide(rideRequest);
        }
        else {
            RideElection rideElection = taxi.getRideElection(rideRequest);
            if (electedTaxiId != taxi.getId()) {
                rideElection.forwardElected(request);
                taxi.removeRideElection(rideElection);
            } else
                RideUtils.electionMaster(taxi, rideElection);
        }
    }
}

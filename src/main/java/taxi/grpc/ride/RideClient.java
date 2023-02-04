package taxi.grpc.ride;

import beans.Position;
import beans.TaxiBean;
import com.seta.taxi.RideServiceGrpc;
import com.seta.taxi.RideServiceGrpc.*;
import com.seta.taxi.RideServiceOuterClass.*;
import debug.Debug;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import taxi.model.RideRequest;
import taxi.model.RideCriteria;
import taxi.model.Taxi;
import utils.GrpcUtils;
import utils.RideUtils;

import java.util.concurrent.TimeUnit;

public class RideClient {

    private final Taxi taxi;

    public RideClient(Taxi taxi) {
        this.taxi = taxi;
    }

    public void sendElection(RideRequest rideRequest) {
        TaxiBean nextTaxi = getNextTaxi(rideRequest);
        if (nextTaxi == null)
            return;

        RideCriteria rideCriteria = taxi.getRideCriteria(rideRequest);

        Election request = Election.newBuilder()
                .setRide(buildRide(rideRequest))
                .setCriteria(Election.Criteria.newBuilder()
                        .setDistance(rideCriteria.getDistance())
                        .setBatteryLevel(rideCriteria.getBatteryLevel())
                        .setTaxiId(rideCriteria.getTaxiId())
                        .build())
                .build();

        System.out.println("[Taxi " + taxi.getId() + "] Sending ELECTION message to Taxi " + nextTaxi.getId() + ':' +
                '\n' + RideUtils.toString(request));

        elect(nextTaxi, request);
    }

    public void forwardElection(Election request) {
        TaxiBean nextTaxi = getNextTaxi(request);
        if (nextTaxi == null)
            return;

        System.out.println("[Taxi " + taxi.getId() + "] Forwarding ELECTION message to Taxi " + nextTaxi.getId() + ':' +
                '\n' + RideUtils.toString(request));

        elect(nextTaxi, request);
    }

    private void elect(TaxiBean nextTaxi, Election request) {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextTaxi.computeSocketAddress())
                .usePlaintext()
                .build();
        RideServiceStub stub = RideServiceGrpc.newStub(channel);

        Context newContext = Context.current().fork();
        Context originalContext = newContext.attach();

        try {
            Debug.sleep();

            stub.elect(request, new StreamObserver<ElectionResponse>() {

                public void onNext(ElectionResponse electionResponse) {
                    int id = electionResponse.getTaxiId();
                    System.out.println("[Taxi " + taxi.getId() + "] Taxi " + id + " received the ELECTION message.");
                }

                public void onError(Throwable throwable) {
                    System.out.println("[Taxi " + taxi.getId() + "] Error! " + throwable.getMessage());
                    GrpcUtils.handleInactiveTaxi(taxi, nextTaxi.getId());
                    forwardElection(request);
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
        } finally {
            newContext.detach(originalContext);
        }
    }

    public void sendElected(RideRequest rideRequest) {
        TaxiBean nextTaxi = getNextTaxi(rideRequest);
        if (nextTaxi == null)
            return;

        Elected request = Elected.newBuilder()
                .setRide(buildRide(rideRequest))
                .setTaxiId(taxi.getId())
                .build();

        System.out.println("[Taxi " + taxi.getId() + "] Sending ELECTED message to Taxi " + nextTaxi.getId() + ':' +
                '\n' + RideUtils.toString(request));

        stopElection(nextTaxi, request);
    }

    public void forwardElected(Elected request) {
        TaxiBean nextTaxi = getNextTaxi(request);
        if (nextTaxi == null)
            return;

        System.out.println("[Taxi " + taxi.getId() + "] Forwarding ELECTED message to Taxi " + nextTaxi.getId() + ':' +
                '\n' + RideUtils.toString(request));

        stopElection(nextTaxi, request);
    }

    private void stopElection(TaxiBean nextTaxi, Elected request) {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextTaxi.computeSocketAddress())
                .usePlaintext()
                .build();
        RideServiceStub stub = RideServiceGrpc.newStub(channel);

        Context newContext = Context.current().fork();
        Context originalContext = newContext.attach();

        try {
            Debug.sleep();

            stub.stopElection(request, new StreamObserver<ElectionResponse>() {

                public void onNext(ElectionResponse electionResponse) {
                    int id = electionResponse.getTaxiId();
                    System.out.println("[Taxi " + taxi.getId() + "] Taxi " + id + " received the ELECTED message.");
                }

                public void onError(Throwable throwable) {
                    System.out.println("[Taxi " + taxi.getId() + "] Error! " + throwable.getMessage());
                    GrpcUtils.handleInactiveTaxi(taxi, nextTaxi.getId());
                    forwardElected(request);
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
        } finally {
            newContext.detach(originalContext);
        }
    }

    private TaxiBean getNextTaxi(RideRequest rideRequest) {
        TaxiBean nextTaxi = taxi.getNextTaxi();
        if (nextTaxi == null) {
            System.out.println("No other taxis.");
            RideUtils.electionMaster(taxi, rideRequest);
        }

        return nextTaxi;
    }

    private TaxiBean getNextTaxi(Election request) {
        return getNextTaxi(new RideRequest(request.getRide()));
    }

    private TaxiBean getNextTaxi(Elected request) {
        return getNextTaxi(new RideRequest(request.getRide()));
    }

    private Ride buildRide(RideRequest rideRequest) {
        return Ride.newBuilder()
                .setId(rideRequest.getId())
                .setStartingPosition(buildPosition(rideRequest.getStartingPosition()))
                .setDestinationPosition(buildPosition(rideRequest.getDestinationPosition()))
                .build();
    }

    private Ride.Position buildPosition(Position position) {
        return Ride.Position.newBuilder()
                .setX(position.getX())
                .setY(position.getY())
                .build();
    }
}

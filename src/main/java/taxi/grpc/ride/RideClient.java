package taxi.grpc.ride;

import beans.TaxiBean;
import com.seta.taxi.RideServiceGrpc;
import com.seta.taxi.RideServiceGrpc.*;
import com.seta.taxi.RideServiceOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import taxi.model.Ride;
import taxi.model.RideCriteria;
import taxi.model.RideElection;
import taxi.model.Taxi;
import utils.RideUtils;

import java.util.concurrent.TimeUnit;

public class RideClient {

    private final Taxi taxi;

    public RideClient(Taxi taxi) {
        this.taxi = taxi;
    }

    public void sendElection(Ride ride) {
        TaxiBean nextTaxi = getNextTaxi(ride);
        if (nextTaxi == null)
            return;

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextTaxi.computeSocketAddress())
                .usePlaintext()
                .build();
        RideServiceStub stub = RideServiceGrpc.newStub(channel);

        RideCriteria rideCriteria = taxi.getRideCriteria(ride);

        Election request = Election.newBuilder()
                .setRide(Election.Ride.newBuilder()
                        .setId(ride.getId())
                        .setStartingPosition(Election.Ride.Position.newBuilder()
                                .setX(ride.getStartingPosition().getX())
                                .setY(ride.getStartingPosition().getY())
                                .build())
                        .setDestinationPosition(Election.Ride.Position.newBuilder()
                                .setX(ride.getDestinationPosition().getX())
                                .setY(ride.getDestinationPosition().getY())
                                .build())
                        .build())
                .setCriteria(Election.Criteria.newBuilder()
                        .setDistance(rideCriteria.getDistance())
                        .setBatteryLevel(rideCriteria.getBatteryLevel())
                        .setTaxiId(rideCriteria.getTaxiId())
                        .build())
                .build();

        System.out.println("[Taxi " + taxi.getId() + "] Sending ELECTION message to Taxi " + nextTaxi.getId() + ':' +
                '\n' + ride +
                '\n' + rideCriteria);

        elect(channel, stub, request);
    }

    public void forwardElection(Election request) {
        TaxiBean nextTaxi = getNextTaxi(request);
        if (nextTaxi == null)
            return;

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextTaxi.computeSocketAddress())
                .usePlaintext()
                .build();
        RideServiceStub stub = RideServiceGrpc.newStub(channel);

        System.out.println("[Taxi " + taxi.getId() + "] Forwarding ELECTION message to Taxi " + nextTaxi.getId() + ':' +
                '\n' + request);

        elect(channel, stub, request);
    }

    private void elect(ManagedChannel channel, RideServiceStub stub, Election request) {
        stub.elect(request, new StreamObserver<ElectionResponse>() {

            public void onNext(ElectionResponse electionResponse) {
                int id = electionResponse.getTaxiId();
                System.out.println("[Taxi " + taxi.getId() + "] Taxi " + id + " received the ELECTION message.");
            }

            public void onError(Throwable throwable) {
                System.out.println("[Taxi " + taxi.getId() + "] Error! " + throwable.getMessage());
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

    public void sendElected(Ride ride) {
        TaxiBean nextTaxi = getNextTaxi(ride);
        if (nextTaxi == null)
            return;

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextTaxi.computeSocketAddress())
                .usePlaintext()
                .build();
        RideServiceStub stub = RideServiceGrpc.newStub(channel);

        Elected request = Elected.newBuilder()
                .setRideId(ride.getId())
                .setTaxiId(taxi.getId())
                .build();

        System.out.println("[Taxi " + taxi.getId() + "] Sending ELECTED message to Taxi " + nextTaxi.getId() + ':' +
                "\nRide ID = " + ride.getId() +
                "\nTaxi ID = " + taxi.getId());

        stopElection(channel, stub, request);
    }

    public void forwardElected(Elected request) {
        TaxiBean nextTaxi = getNextTaxi(request);
        if (nextTaxi == null)
            return;

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextTaxi.computeSocketAddress())
                .usePlaintext()
                .build();
        RideServiceStub stub = RideServiceGrpc.newStub(channel);

        System.out.println("[Taxi " + taxi.getId() + "] Forwarding ELECTED message to Taxi " + nextTaxi.getId() + ':' +
                '\n' + request);

        stopElection(channel, stub, request);
    }

    private void stopElection(ManagedChannel channel, RideServiceStub stub, Elected request) {
        stub.stopElection(request, new StreamObserver<ElectionResponse>() {

            public void onNext(ElectionResponse electionResponse) {
                int id = electionResponse.getTaxiId();
                System.out.println("[Taxi " + taxi.getId() + "] Taxi " + id + " received the ELECTED message.");
            }

            public void onError(Throwable throwable) {
                System.out.println("[Taxi " + taxi.getId() + "] Error! " + throwable.getMessage());
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

    private TaxiBean getNextTaxi(Ride ride) {
        TaxiBean nextTaxi = taxi.getNextTaxi();
        if (nextTaxi == null) {
            System.out.println("No other taxis.");
            RideUtils.electionMaster(taxi, ride);
        }

        return nextTaxi;
    }

    private TaxiBean getNextTaxi(Election request) {
        return getNextTaxi(new Ride(request.getRide()));
    }

    private TaxiBean getNextTaxi(Elected request) {
        return getNextTaxi(new Ride(request.getRideId()));
    }
}

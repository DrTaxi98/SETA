package taxi.grpc.recharge;

import beans.OtherTaxisSet;
import beans.TaxiBean;
import com.seta.taxi.RechargeServiceGrpc;
import com.seta.taxi.RechargeServiceGrpc.*;
import com.seta.taxi.RechargeServiceOuterClass.*;
import debug.Debug;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import taxi.model.Taxi;
import utils.GrpcUtils;
import utils.RechargeUtils;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;

public class RechargeClient {

    private final Taxi taxi;

    public RechargeClient(Taxi taxi) {
        this.taxi = taxi;
    }

    public void recharge() {
        System.out.println("[Taxi " + taxi.getId() + "] Starting Lamport's algorithm...");
        lamport(taxi.getId());

        OtherTaxisSet otherTaxisSet = taxi.getOtherTaxisSet();
        System.out.println("[Taxi " + taxi.getId() + "] " + otherTaxisSet);
        SortedSet<TaxiBean> otherTaxis = otherTaxisSet.getOtherTaxis();
        otherTaxis.add(taxi.getTaxiBean());

        Recharge request = Recharge.newBuilder()
                .setDistrict(taxi.getDistrict())
                .setTaxiId(taxi.getId())
                .setTimestamp(taxi.getTimestamp())
                .build();

        taxi.setRechargeRequest(request);

        System.out.println("[Taxi " + taxi.getId() + "] Broadcasting RECHARGE message:" +
                '\n' + RechargeUtils.toStringRecharge(request));

        ArrayList<Thread> threads = new ArrayList<>();
        for (TaxiBean otherTaxi : otherTaxis) {
            RechargeThread rechargeThread = new RechargeThread(taxi, otherTaxi, request, otherTaxis.size());
            threads.add(rechargeThread);
            rechargeThread.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        taxi.recharge();
    }

    public void lamport(int startTaxiId) {
        TaxiBean nextTaxi = taxi.getNextTaxi();
        if (nextTaxi == null) {
            System.out.println("No other taxis: no need to adjust the clock.");
            return;
        } else if (nextTaxi.getId() == startTaxiId) {
            return;
        }

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextTaxi.computeSocketAddress())
                .usePlaintext()
                .build();

        RechargeServiceStub stub = RechargeServiceGrpc.newStub(channel);

        LamportRequest request = LamportRequest.newBuilder()
                .setStartTaxiId(startTaxiId)
                .setTimestamp(taxi.getTimestamp())
                .build();

        System.out.println("[Taxi " + taxi.getId() + "] Sending LAMPORT message to Taxi " + nextTaxi.getId() + ':' +
                '\n' + RechargeUtils.toStringLamport(request));

        Context newContext = Context.current().fork();
        Context originalContext = newContext.attach();

        try {
            Debug.sleep();

            stub.lamport(request, new StreamObserver<Timestamp>() {

                @Override
                public void onNext(Timestamp timestamp) {
                    long thisTimestamp = taxi.getTimestamp();
                    long otherTimestamp = timestamp.getTimestamp();
                    System.out.println("[Taxi " + taxi.getId() + "] Taxi " + nextTaxi.getId() + " answered.");
                    taxi.adjustTimestamp(thisTimestamp, otherTimestamp);
                }

                public void onError(Throwable throwable) {
                    System.out.println("[Taxi " + taxi.getId() + "] Error! " + throwable.getMessage());
                    GrpcUtils.handleInactiveTaxi(taxi, nextTaxi.getId());
                    lamport(startTaxiId);
                    channel.shutdownNow();
                }

                public void onCompleted() {
                    channel.shutdownNow();
                }
            });

            try {
                channel.awaitTermination(20L * taxi.getOtherTaxisSet().getOtherTaxis().size(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            newContext.detach(originalContext);
        }
    }
}

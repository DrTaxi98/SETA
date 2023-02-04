package taxi.grpc.recharge;

import beans.TaxiBean;
import com.seta.taxi.RechargeServiceGrpc;
import com.seta.taxi.RechargeServiceGrpc.*;
import com.seta.taxi.RechargeServiceOuterClass.*;
import debug.Debug;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import taxi.model.Taxi;
import utils.GrpcUtils;

import java.util.concurrent.TimeUnit;

public class RechargeThread extends Thread {

    private final Taxi taxi;
    private final TaxiBean otherTaxi;
    private final Recharge request;
    private final long awaitTerminationTime;

    public RechargeThread(Taxi taxi, TaxiBean otherTaxi, Recharge request, int otherTaxisSize) {
        this.taxi = taxi;
        this.otherTaxi = otherTaxi;
        this.request = request;
        awaitTerminationTime = otherTaxisSize * Taxi.RECHARGE_TIME;
    }

    @Override
    public void run() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(otherTaxi.computeSocketAddress())
                .usePlaintext()
                .build();
        RechargeServiceStub stub = RechargeServiceGrpc.newStub(channel);

        Debug.sleep();

        stub.recharge(request, new StreamObserver<RechargeOk>() {

            public void onNext(RechargeOk rechargeOk) {
                int id = rechargeOk.getTaxiId();
                System.out.println("[Taxi " + taxi.getId() + "] Taxi " + id + " answered OK.");
            }

            public void onError(Throwable throwable) {
                System.out.println("[Taxi " + taxi.getId() + "] Error! " + throwable.getMessage());
                GrpcUtils.handleInactiveTaxi(taxi, otherTaxi.getId());
                channel.shutdownNow();
            }

            public void onCompleted() {
                channel.shutdownNow();
            }
        });

        try {
            channel.awaitTermination(awaitTerminationTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

package taxi.grpc.recharge;

import com.seta.taxi.RechargeServiceGrpc.*;
import com.seta.taxi.RechargeServiceOuterClass.*;
import io.grpc.stub.StreamObserver;
import taxi.model.Taxi;
import utils.RechargeUtils;

public class RechargeServiceImpl extends RechargeServiceImplBase {

    private final Taxi taxi;

    public RechargeServiceImpl(Taxi taxi) {
        this.taxi = taxi;
    }

    @Override
    public void recharge(Recharge request, StreamObserver<RechargeOk> responseObserver) {
        System.out.println("[Taxi " + taxi.getId() + "] Received RECHARGE message:" +
                '\n' + RechargeUtils.toStringRecharge(request));

        int district = request.getDistrict();
        int otherTaxiId = request.getTaxiId();

        Taxi.Status status = taxi.getStatus();
        if (taxi.getDistrict() != district ||
                (status != Taxi.Status.RECHARGING && status != Taxi.Status.TRYING_TO_RECHARGE)) {
            System.out.println("[Taxi " + taxi.getId() + "] Not interested.");
            sendRechargeOkResponse(responseObserver, otherTaxiId);
        } else if (status == Taxi.Status.RECHARGING) {
            System.out.println("[Taxi " + taxi.getId() + "] Recharging.");
            addWaitAndSend(request, responseObserver, otherTaxiId);
        }
        else {
            if (taxi.getId() == otherTaxiId) {
                sendRechargeOkResponse(responseObserver, otherTaxiId);
            }
            else {
                long timestamp = taxi.getRechargeRequest().getTimestamp();
                long otherTimestamp = request.getTimestamp();
                System.out.println("[Taxi " + taxi.getId() + "] Timestamp: " + timestamp);
                if (otherTimestamp < timestamp) {
                    sendRechargeOkResponse(responseObserver, otherTaxiId);
                }
                else {
                    addWaitAndSend(request, responseObserver, otherTaxiId);
                }
            }
        }
    }

    @Override
    public void lamport(LamportRequest request, StreamObserver<Timestamp> responseObserver) {
        long thisTimestamp = taxi.getTimestamp();
        int startTaxiId = request.getStartTaxiId();
        long otherTimestamp = request.getTimestamp();

        System.out.println("[Taxi " + taxi.getId() + "] Received LAMPORT message.");

        taxi.adjustTimestamp(thisTimestamp, otherTimestamp);
        taxi.lamport(startTaxiId);

        long timestamp = taxi.getTimestamp();
        Timestamp response = Timestamp.newBuilder()
                .setTimestamp(timestamp)
                .build();

        System.out.println("[Taxi " + taxi.getId() + "] Answering with Timestamp: " + timestamp);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void sendRechargeOkResponse(StreamObserver<RechargeOk> responseObserver, int otherTaxiId) {
        RechargeOk response = RechargeOk.newBuilder()
                .setTaxiId(taxi.getId())
                .build();

        System.out.println("[Taxi " + taxi.getId() + "] Sending OK response to Taxi " + otherTaxiId);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void addWaitAndSend(Recharge request, StreamObserver<RechargeOk> responseObserver, int otherTaxiId) {
        taxi.addRechargeRequest(request);
        System.out.println("[Taxi " + taxi.getId() + "] Added Taxi " + otherTaxiId + " to the queue.");
        taxi.waitForRecharged();
        sendRechargeOkResponse(responseObserver, otherTaxiId);
    }
}

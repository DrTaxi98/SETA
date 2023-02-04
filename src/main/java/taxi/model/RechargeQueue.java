package taxi.model;

import com.seta.taxi.RechargeServiceOuterClass.*;

import java.util.ArrayList;
import java.util.List;

public class RechargeQueue {

    private final List<Recharge> rechargeRequests = new ArrayList<>();
    private int waiting = 0;
    private boolean recharged = false;

    public synchronized void add(Recharge rechargeRequest) {
        rechargeRequests.add(rechargeRequest);
    }

    private synchronized List<Recharge> getAllAndClear() {
        List<Recharge> recharges = new ArrayList<>(rechargeRequests);
        rechargeRequests.clear();
        return recharges;
    }

    public synchronized void waitForRecharged() {
        while (!recharged) {
            try {
                waiting++;
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        waiting--;
        if (waiting == 0) {
            getAllAndClear();
            recharged = false;
        }
    }

    public synchronized void setRecharged() {
        recharged = true;
        notifyAll();
    }
}

package utils;

import com.seta.taxi.RechargeServiceOuterClass.*;

public class RechargeUtils {

    public static String toStringRecharge(Recharge recharge) {
        return "Recharging station in district: " + recharge.getDistrict() +
                "\nTaxi ID = " + recharge.getTaxiId() +
                "\nTimestamp = " + recharge.getTimestamp();
    }

    public static String toStringLamport(LamportRequest lamportRequest) {
        return "Starting Taxi ID = " + lamportRequest.getStartTaxiId() +
                "\nTimestamp = " + lamportRequest.getTimestamp();
    }
}

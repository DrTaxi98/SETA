package utils;

import taxi.model.Taxi;

public class GrpcUtils {

    public static void handleInactiveTaxi(Taxi taxi, int inactiveTaxiId) {
        System.out.println("[Taxi " + taxi.getId() + "] Taxi " + inactiveTaxiId + " is no longer active.");
        System.out.println("[Taxi " + taxi.getId() + "] Removing Taxi " + inactiveTaxiId);
        taxi.removeOtherTaxi(inactiveTaxiId);
        System.out.println("[Taxi " + taxi.getId() + "] Taxi " + inactiveTaxiId + " removed.");
        System.out.println("[Taxi " + taxi.getId() + "] " + taxi.getOtherTaxisSet());
    }
}

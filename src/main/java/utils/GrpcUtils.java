package utils;

import taxi.model.Taxi;

public class GrpcUtils {

    public static void handleInactiveTaxi(Taxi taxi, int inactiveTaxiId) {
        System.out.println("[Taxi " + taxi.getId() + "] Taxi " + inactiveTaxiId + " is no longer active.");
        System.out.println("[Taxi " + taxi.getId() + "] " +
                "Removing Taxi " + inactiveTaxiId + " from the list of other taxis...");
        taxi.removeOtherTaxi(inactiveTaxiId);
        System.out.println("[Taxi " + taxi.getId() + "] Taxi " + inactiveTaxiId + " removed.");
        System.out.println("[Taxi " + taxi.getId() + "] " + taxi.getOtherTaxisSet());

        taxi.removeOtherTaxiFromServer(inactiveTaxiId);
    }
}

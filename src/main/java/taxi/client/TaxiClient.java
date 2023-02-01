package taxi.client;

import beans.TaxiBean;
import taxi.model.Taxi;

import java.util.Random;
import java.util.Scanner;

public class TaxiClient {

    private static final int MAX_TAXI = 10;
    private static final int MIN_PORT = 9000;
    private static final int MAX_PORT = 9100;

    private static final String ipAddress = "localhost";
    private static final String administratorServerAddress = "http://localhost:1337";

    private static final String rechargeString = "recharge";
    private static final String quitString = "quit";

    public static void main(String[] args) {
        Random random = new Random();
        int id = random.nextInt(MAX_TAXI);
        int portNumber = random.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;

        TaxiBean taxiBean = new TaxiBean(id, ipAddress, portNumber);
        System.out.println("Initializing " + taxiBean);
        Taxi taxi = new Taxi(id, ipAddress, portNumber, administratorServerAddress);
        System.out.println("Taxi initialized.");

        if (!taxi.start()) {
            System.out.println("Stopping taxi client...");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        String command = "";

        while (!command.equalsIgnoreCase(quitString)) {
            System.out.println("\nCommands available:" +
                    "\n\t\"" + rechargeString + "\": recharge the battery of the taxi" +
                    "\n\t\"" + quitString + "\": leave the system" +
                    "\n");
            command = scanner.nextLine();
            if (command.equalsIgnoreCase(rechargeString))
                taxi.recharge();
            else if (!command.equalsIgnoreCase(quitString)) {
                System.out.println("Invalid command: \"" + command + '\"');
            }
        }

        taxi.quit();
    }
}

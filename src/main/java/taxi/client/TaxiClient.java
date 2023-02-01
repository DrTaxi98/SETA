package taxi.client;

import beans.TaxiBean;
import exceptions.RestException;
import taxi.model.Taxi;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class TaxiClient {

    private static final int MAX_TAXI = 10;
    private static final int PORT_NUMBER = 0;

    private static final String IP_ADDRESS = "localhost";
    private static final String ADMINISTRATOR_SERVER_ADDRESS = "http://localhost:1337";

    private static final String RECHARGE_STRING = "recharge";
    private static final String QUIT_STRING = "quit";

    public static void main(String[] args) {
        Random random = new Random();
        int id = random.nextInt(MAX_TAXI);

        Taxi taxi = new Taxi(id, IP_ADDRESS, PORT_NUMBER, ADMINISTRATOR_SERVER_ADDRESS);
        System.out.println("Taxi initialized.");

        try {
            taxi.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Stopping taxi client...");
            return;
        } catch (RestException e) {
            System.out.println(e.getMessage());
            System.out.println("Stopping taxi client...");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        String command = "";

        while (!command.equalsIgnoreCase(QUIT_STRING)) {
            System.out.println("\nCommands available:" +
                    "\n\t\"" + RECHARGE_STRING + "\": recharge the battery of the taxi" +
                    "\n\t\"" + QUIT_STRING + "\": leave the system" +
                    "\n");
            command = scanner.nextLine();
            if (command.equalsIgnoreCase(RECHARGE_STRING))
                taxi.recharge();
            else if (!command.equalsIgnoreCase(QUIT_STRING)) {
                System.out.println("Invalid command: \"" + command + '\"');
            }
        }

        taxi.quit();
    }
}

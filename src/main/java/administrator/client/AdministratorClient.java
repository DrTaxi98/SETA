package administrator.client;

import beans.AverageStatistics;
import beans.TaxisSet;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.Response;
import java.util.Scanner;

public class AdministratorClient {

    private static final String serverAddress = "http://localhost:1337";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Client client = Client.create();

        int input = 0;

        while (input != 4) {
            System.out.println("\nAdministrator Client Menu");
            System.out.println("1: List of the taxis currently located in the smart city");
            System.out.println("2: Average of the last n local statistics of a taxi");
            System.out.println("3: Average of the statistics provided by all the taxis " +
                    "and occurred from timestamps t1 and t2");
            System.out.println("4: Quit");
            System.out.print("Insert an option from the menu: ");

            if (scanner.hasNextInt())
                input = scanner.nextInt();
            else {
                scanner.next();
                input = 0;
            }

            switch (input) {
                case 1:
                    getTaxisList(client);
                    break;
                case 2:
                    getTaxiAverageStatistics(client);
                    break;
                case 3:
                    getTimestampsAverageStatistics(client);
                    break;
                case 4:
                    break;
                default:
                    System.out.println("Invalid option. Please insert an option from the menu.");
            }
        }

        System.out.println("Administrator Client stopped.");
    }

    private static void getTaxisList(Client client) {
        String path = "/taxis";
        ClientResponse clientResponse = getRequest(client, serverAddress + path);
        if (clientResponse != null) {
            System.out.println(clientResponse);
            TaxisSet taxisSet = clientResponse.getEntity(TaxisSet.class);
            System.out.println(taxisSet);
        }
    }

    private static void getTaxiAverageStatistics(Client client) {
        int id = getIntInput("taxi id");
        int n = getIntInput("n");

        String path = "/statistics/" + id + "/average" + "?n=" + n;
        getAverageStatistics(client, path);
    }

    private static void getTimestampsAverageStatistics(Client client) {
        long t1 = getLongInput("t1");
        long t2 = getLongInput("t2");

        String path = "/statistics/average" + "?t1=" + t1 + "&t2=" + t2;
        getAverageStatistics(client, path);
    }

    private static void getAverageStatistics(Client client, String path) {
        ClientResponse clientResponse = getRequest(client, serverAddress + path);
        if (clientResponse != null) {
            System.out.println(clientResponse);
            if (clientResponse.getStatus() == Response.Status.NOT_FOUND.getStatusCode())
                System.out.println("No statistics available.");
            else if (clientResponse.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
                String message = clientResponse.getEntity(String.class);
                System.out.println(message);
            }
            else {
                AverageStatistics stats = clientResponse.getEntity(AverageStatistics.class);
                System.out.println(stats);
            }
        }
    }

    private static ClientResponse getRequest(Client client, String url) {
        WebResource webResource = client.resource(url);
        try {
            return webResource.type("application/json").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            System.out.println("Server not available");
            return null;
        }
    }

    private static int getIntInput(String paramName) {
        int input = 0;
        boolean isInputValid = false;

        do {
            System.out.print("Insert " + paramName + ": ");
            if (scanner.hasNextInt()) {
                input = scanner.nextInt();
                isInputValid = true;
            }
            else {
                scanner.next();
                System.out.println("Please insert an integer.");
            }
        } while (!isInputValid);

        return input;
    }

    private static long getLongInput(String paramName) {
        long input = 0;
        boolean isInputValid = false;

        do {
            System.out.print("Insert " + paramName + ": ");
            if (scanner.hasNextLong()) {
                input = scanner.nextLong();
                isInputValid = true;
            }
            else {
                scanner.next();
                System.out.println("Please insert a long integer.");
            }
        } while (!isInputValid);

        return input;
    }
}

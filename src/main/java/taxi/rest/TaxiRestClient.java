package taxi.rest;

import beans.LocalStatistics;
import beans.TaxiBean;
import beans.TaxiStartInfo;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import utils.RestUtils;

import javax.ws.rs.core.Response;

public class TaxiRestClient {

    private final Client client = Client.create();
    private final Gson gson = new Gson();

    private final String serverAddress;

    public TaxiRestClient(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public TaxiStartInfo addTaxi(TaxiBean taxi) {
        String path = "/taxis/add";
        String input = gson.toJson(taxi);
        ClientResponse clientResponse = RestUtils.postRequest(client, serverAddress + path, input);

        if (clientResponse == null)
            return null;

        System.out.println(clientResponse);

        if (clientResponse.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
            String message = clientResponse.getEntity(String.class);
            System.out.println(message);
            return null;
        }

        TaxiStartInfo taxiStartInfo = clientResponse.getEntity(TaxiStartInfo.class);
        System.out.println(taxiStartInfo);
        return taxiStartInfo;
    }

    public boolean removeTaxi(int id) {
        String path = "/taxis/" + id + "/remove";
        ClientResponse clientResponse = RestUtils.deleteRequest(client, serverAddress + path);

        if (clientResponse == null)
            return false;

        System.out.println(clientResponse);

        if (clientResponse.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            System.out.println("Taxi does not exist.");
            return false;
        }

        return true;
    }

    public boolean addStatistics(LocalStatistics stats) {
        String path = "/statistics/add";
        String input = gson.toJson(stats);
        ClientResponse clientResponse = RestUtils.postRequest(client, serverAddress + path, input);

        if (clientResponse == null)
            return false;

        System.out.println(clientResponse);

        if (clientResponse.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
            String message = clientResponse.getEntity(String.class);
            System.out.println(message);
            return false;
        }

        return true;
    }
}

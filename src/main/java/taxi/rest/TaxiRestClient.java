package taxi.rest;

import beans.LocalStatistics;
import beans.TaxiBean;
import beans.TaxiStartInfo;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import exceptions.RestException;
import utils.RestUtils;

import javax.ws.rs.core.Response;

public class TaxiRestClient {

    private final Client client = Client.create();
    private final Gson gson = new Gson();

    private final String serverAddress;

    public TaxiRestClient(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public TaxiStartInfo addTaxi(TaxiBean taxi) throws RestException {
        String path = "/taxis/add";
        String input = gson.toJson(taxi);
        ClientResponse clientResponse = RestUtils.postRequest(client, serverAddress + path, input);

        if (clientResponse == null)
            throw new RestException("An error occurred during registration.");

        System.out.println(clientResponse);

        if (clientResponse.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
            String message = clientResponse.getEntity(String.class);
            throw new RestException(message);
        }

        TaxiStartInfo taxiStartInfo = clientResponse.getEntity(TaxiStartInfo.class);
        System.out.println(taxiStartInfo);
        return taxiStartInfo;
    }

    public void removeTaxi(int id) throws RestException {
        String path = "/taxis/" + id + "/remove";
        ClientResponse clientResponse = RestUtils.deleteRequest(client, serverAddress + path);

        if (clientResponse == null)
            throw new RestException("An error occurred during taxi removal.");

        System.out.println(clientResponse);

        if (clientResponse.getStatus() == Response.Status.NOT_FOUND.getStatusCode())
            throw new RestException("Taxi does not exist.");
    }

    public void addStatistics(LocalStatistics stats) throws RestException {
        String path = "/statistics/add";
        String input = gson.toJson(stats);
        ClientResponse clientResponse = RestUtils.postRequest(client, serverAddress + path, input);

        if (clientResponse == null)
            throw new RestException("An error occurred during statistics insertion.");

        System.out.println(clientResponse);

        if (clientResponse.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
            String message = clientResponse.getEntity(String.class);
            throw new RestException(message);
        }
    }
}

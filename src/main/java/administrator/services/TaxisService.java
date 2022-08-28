package administrator.services;

import beans.AverageStatistics;
import beans.TaxiBean;
import beans.TaxiStartInfo;
import beans.TaxisList;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("taxis")
public class TaxisService {

    @GET
    @Produces({"application/json", "application/xml"})
    public Response getTaxisList() {
        TaxisList taxis = TaxisList.getInstance();
        return Response.ok(taxis).build();
    }

    @Path("add")
    @POST
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response addTaxi(TaxiBean taxi) {
        TaxisList.getInstance().add(taxi);
        TaxiStartInfo taxiStartInfo = new TaxiStartInfo();
        return Response.ok(taxiStartInfo).build();
    }

    @Path("{id}/remove")
    @DELETE
    public Response removeTaxi(@PathParam("id") int id) {
        TaxisList.getInstance().remove(id);
        return Response.ok().build();
    }

    @Path("{id}/statistics")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response receiveStatistics(@PathParam("id") int id, AverageStatistics stats) {
        return Response.ok().build();
    }
}

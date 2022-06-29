package administrator.services;

import administrator.beans.StatisticsBean;
import administrator.beans.TaxiBean;
import administrator.beans.TaxisListBean;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("taxis")
public class TaxisService {

    @GET
    @Produces({"application/json", "application/xml"})
    public Response getTaxisList() {
        TaxisListBean taxis = TaxisListBean.getInstance();
        return Response.ok(taxis).build();
    }

    @Path("add")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response addTaxi(TaxiBean taxi) {
        return Response.ok().build();
    }

    @Path("{id}/remove")
    @DELETE
    public Response removeTaxi(@PathParam("id") String id) {
        return Response.ok().build();
    }

    @Path("{id}/statistics")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response receiveStatistics(@PathParam("id") String id, StatisticsBean stats) {
        return Response.ok().build();
    }
}

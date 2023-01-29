package administrator.services;

import beans.TaxiBean;
import beans.TaxiStartInfo;
import beans.TaxisSet;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("taxis")
public class TaxisService {

    @GET
    @Produces({"application/json", "application/xml"})
    public Response getTaxis() {
        return Response.ok(TaxisSet.getInstance()).build();
    }

    @Path("add")
    @POST
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response addTaxi(TaxiBean taxi) {
        TaxiStartInfo taxiStartInfo = TaxisSet.getInstance().add(taxi);
        if (taxiStartInfo != null)
            return Response.ok(taxiStartInfo).build();
        else
            return Response.status(Response.Status.BAD_REQUEST).entity("Taxi already exists").build();
    }

    @Path("{id}/remove")
    @DELETE
    public Response removeTaxi(@PathParam("id") int id) {
        if (TaxisSet.getInstance().remove(id))
            return Response.ok().build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }
}

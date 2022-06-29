package administrator.services;

import administrator.beans.StatisticsBean;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("statistics")
public class StatisticsService {

    @Path("average/{id}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getAverage(@PathParam("id") String id, @QueryParam("n") int n) {
        StatisticsBean stats = null;
        if (stats != null)
            return Response.ok(stats).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Path("average")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getAverage(@QueryParam("t1") int t1, @QueryParam("t2") int t2) {
        StatisticsBean stats = null;
        return Response.ok(stats).build();
    }
}

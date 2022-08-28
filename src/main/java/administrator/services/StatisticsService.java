package administrator.services;

import beans.AverageStatistics;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("statistics")
public class StatisticsService {

    @Path("average/{id}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getAverage(@PathParam("id") String id, @QueryParam("n") int n) {
        AverageStatistics stats = null;
        if (stats != null)
            return Response.ok(stats).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Path("average")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getAverage(@QueryParam("t1") long t1, @QueryParam("t2") long t2) {
        AverageStatistics stats = null;
        return Response.ok(stats).build();
    }
}

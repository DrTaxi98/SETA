package administrator.services;

import administrator.model.StatisticsMap;
import beans.AverageStatistics;
import beans.LocalStatistics;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("statistics")
public class StatisticsService {

    @Path("add")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response addStatistics(LocalStatistics stats) {
        Boolean added = StatisticsMap.getInstance().add(stats);
        if (added == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        else if (added)
            return Response.ok().build();
        else
            return Response.status(Response.Status.CONFLICT).build();
    }

    @Path("{id}/average")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getTaxiAverageStatistics(@PathParam("id") int id, @QueryParam("n") int n) {
        AverageStatistics stats = StatisticsMap.getInstance().getTaxiAverage(id, n);
        if (stats != null)
            return Response.ok(stats).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Path("average")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getAverageStatistics(@QueryParam("t1") long t1, @QueryParam("t2") long t2) {
        AverageStatistics stats = StatisticsMap.getInstance().getAverage(t1, t2);
        if (stats != null)
            return Response.ok(stats).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }
}

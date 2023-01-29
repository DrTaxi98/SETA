package administrator.services;

import administrator.exceptions.InvalidParameterException;
import administrator.model.StatisticsSet;
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
        if (StatisticsSet.getInstance().add(stats))
            return Response.ok().build();
        else
            return Response.status(Response.Status.BAD_REQUEST).entity("Statistics already exist").build();
    }

    @Path("{id}/average")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getTaxiAverageStatistics(@PathParam("id") int id, @QueryParam("n") int n) {
        try {
            AverageStatistics stats = StatisticsSet.getInstance().getTaxiAverage(id, n);
            if (stats != null)
                return Response.ok(stats).build();
            else
                return Response.status(Response.Status.NOT_FOUND).build();
        } catch (InvalidParameterException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @Path("average")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getTimestampsAverageStatistics(@QueryParam("t1") long t1, @QueryParam("t2") long t2) {
        try {
            AverageStatistics stats = StatisticsSet.getInstance().getTimestampsAverage(t1, t2);
            if (stats != null)
                return Response.ok(stats).build();
            else
                return Response.status(Response.Status.NOT_FOUND).build();
        } catch (InvalidParameterException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}

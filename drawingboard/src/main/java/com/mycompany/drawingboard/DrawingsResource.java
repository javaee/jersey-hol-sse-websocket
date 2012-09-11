package com.mycompany.drawingboard;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.glassfish.jersey.media.sse.EventChannel;

@Path("drawings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DrawingsResource {
    @GET
    @Path("events")
    @Produces(EventChannel.SERVER_SENT_EVENTS)
    public EventChannel getEvents() {
        EventChannel ec = new EventChannel();
        DataProvider.registerListener(ec);
        return ec;
    }
    
    @GET
    public List<Drawing> get() {
        return DataProvider.allDrawings();
    }
    
    @POST
    public Response create(@Context UriInfo uriInfo, Drawing drawing) {
        return Response.created(uriInfo.getBaseUriBuilder()
                .path(DrawingsResource.class, "getDrawing")
                .build(DataProvider.newDrawing(drawing))
            ).build();
    }
    
    @Path("{id:[0-9]+}")
    public DrawingResource getDrawing(@PathParam("id") int drawingId) {
        return new DrawingResource(drawingId);
    }
}

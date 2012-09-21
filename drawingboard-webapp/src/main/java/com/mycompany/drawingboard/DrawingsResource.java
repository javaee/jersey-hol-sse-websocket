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

/**
 * JAX-RS root resource exposing RESTful interface to access drawings.
 */
@Path("drawings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DrawingsResource {
    /**
     * Streams server-sent events.
     * @return Long-running response in form of an event channel.
     */
    @GET
    @Path("events")
    @Produces(EventChannel.SERVER_SENT_EVENTS)
    public EventChannel getEvents() {
        EventChannel ec = new EventChannel();
        DataProvider.addEventChannel(ec);
        return ec;
    }
    
    /**
     * Retrieves a list of all drawings.
     * @return List of all drawings.
     */
    @GET
    public List<Drawing> get() {
        return DataProvider.getAllDrawings();
    }
    
    /**
     * Creates a new drawing.
     * @param uriInfo JAX-RS UriInfo instance (injected by the JAX-RS runtime).
     * @param Drawing that comes in the message entity (injected by the JAX-RS
     *                runtime).
     * @return HTTP response 201 (created) with the location header pointing to
     *         the newly created drawing resource.
     */
    @POST
    public Response create(@Context UriInfo uriInfo, Drawing drawing) {
        return Response.created(uriInfo.getBaseUriBuilder()
                .path(DrawingsResource.class, "getDrawing")
                .build(DataProvider.createDrawing(drawing))
            ).build();
    }

    /**
     * Returns a sub-resource corresponding to the drawing with the give drawing ID.
     * @param drawingId ID of the drawing to be retrieved.
     * @return Sub-resource corresponding to the drawing.
     */
    @Path("{id:[0-9]+}")
    public DrawingResource getDrawing(@PathParam("id") int drawingId) {
        return new DrawingResource(drawingId);
    }
}

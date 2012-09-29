package com.mycompany.drawingboard;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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
                .path(DrawingsResource.class).path("{id}")
                .build(DataProvider.createDrawing(drawing))
            ).build();
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
     * Deletes a drawing.
     * @param drawingId ID of the drawing to be deleted.
     */
    @Path("{id:[0-9]+}")
    @DELETE
    @Consumes("*/*")
    public void delete(@PathParam("id") int drawingId) {
        if (!DataProvider.deleteDrawing(drawingId)) {
            throw new NotFoundException();
        }
    }

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
}

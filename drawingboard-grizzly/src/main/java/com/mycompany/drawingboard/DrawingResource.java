package com.mycompany.drawingboard;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Sub-resource exposing RESTful API enabling CRUD operations for a drawing.
 * This sub-resource is mapped to "drawings/{drawingId}" path
 * (see {@link DrawingsResource#getDrawing(int)}).
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DrawingResource {
    private final int drawingId;
    
    DrawingResource(int drawingId) {
        this.drawingId = drawingId;
    }

    /**
     * Retrieves a single drawing as a JSON object.
     */
    @GET
    public Drawing get() {
        Drawing drawing = DataProvider.getDrawing(drawingId);
        if (drawing == null) {
            throw new WebApplicationException(404);
        }
        return drawing;
    }
    
    /**
     * Creates/updates a drawing.
     * @param uriInfo JAX-RS UriInfo instance (injected by the JAX-RS runtime).
     * @param Drawing that comes in the message entity (injected by the JAX-RS
     *                runtime).
     */
    @PUT
    public Response put(@Context UriInfo uriInfo, Drawing drawing) {
        if (drawing.id != drawingId) {
            throw new WebApplicationException(401);
        }
        Response.ResponseBuilder rb;
        if (DataProvider.updateDrawing(drawing)) {
            rb = Response.created(uriInfo.getBaseUriBuilder()
                    .path(DrawingsResource.class, "getNote")
                    .build(drawingId));
        } else {
            rb = Response.ok();
        }
        return rb.entity(drawing).type(MediaType.APPLICATION_JSON).build();
    }
    
    /**
     * Deletes a drawing.
     */
    @DELETE
    @Consumes("*/*")
    public void delete() {
        if (!DataProvider.deleteDrawing(drawingId)) {
            throw new WebApplicationException(404);
        }
    }
}

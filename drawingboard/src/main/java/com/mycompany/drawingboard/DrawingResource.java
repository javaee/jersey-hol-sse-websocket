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

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DrawingResource {
    private final int drawingId;
    
    DrawingResource(int drawingId) {
        this.drawingId = drawingId;
    }

    @GET
    public Drawing get() {
        Drawing drawing = DataProvider.getById(drawingId);
        if (drawing == null) {
            throw new WebApplicationException(404);
        }
        return drawing;
    }
    
    @PUT
    public Response put(@Context UriInfo uriInfo, Drawing drawing) {
        if (drawing.id != drawingId) {
            throw new WebApplicationException(401);
        }
        Response.ResponseBuilder rb;
        if (DataProvider.put(drawing)) {
            rb = Response.created(uriInfo.getBaseUriBuilder()
                    .path(DrawingsResource.class, "getNote")
                    .build(drawingId));
        } else {
            rb = Response.ok();
        }
        return rb.entity(drawing).type(MediaType.APPLICATION_JSON).build();
    }
    
    @DELETE
    @Consumes("*/*")
    public void delete() {
        if (!DataProvider.deleteById(drawingId)) {
            throw new WebApplicationException(404);
        }
    }
}

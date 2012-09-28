package com.mycompany.drawingboard;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

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

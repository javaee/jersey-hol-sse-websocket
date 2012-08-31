package com.mycompany.notebook;

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
public class NoteResource {
    private final int noteId;
    
    NoteResource(int noteId) {
        this.noteId = noteId;
    }

    @GET
    public Note get() {
        Note note = DataProvider.getById(noteId);
        if (note == null) {
            throw new WebApplicationException(404);
        }
        return note;
    }
    
    @PUT
    public Response put(@Context UriInfo uriInfo, Note note) {
        if (note.id != noteId) {
            throw new WebApplicationException(401);
        }
        Response.ResponseBuilder rb;
        if (DataProvider.put(note)) {
            rb = Response.created(uriInfo.getBaseUriBuilder()
                    .path(NotesResource.class, "getNote")
                    .build(noteId));
        } else {
            rb = Response.ok();
        }
        return rb.entity(note).type(MediaType.APPLICATION_JSON).build();
    }
    
    @DELETE
    public void delete() {
        if (!DataProvider.deleteById(noteId)) {
            throw new WebApplicationException(404);
        }
    }
}

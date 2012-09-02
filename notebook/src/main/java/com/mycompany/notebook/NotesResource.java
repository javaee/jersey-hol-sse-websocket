/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.notebook;

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
 *
 * @author martin
 */
@Path("notes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NotesResource {
    @GET
    @Path("events")
    @Produces(EventChannel.SERVER_SENT_EVENTS)
    public EventChannel getEvents() {
        EventChannel ec = new EventChannel();
        DataProvider.registerListener(ec);
        return ec;
    }
    
    @GET
    public List<Note> get() {
        return DataProvider.allNotes();
    }
    
    @POST
    public Response create(@Context UriInfo uriInfo, Note note) {
        return Response.created(uriInfo.getBaseUriBuilder()
                .path(NotesResource.class, "getNote")
                .build(DataProvider.newNote(note))
                ).build();
    }
    
    @Path("{id:[0-9]+}")
    public NoteResource getNote(@PathParam("id") int noteId) {
        return new NoteResource(noteId);
    }
}

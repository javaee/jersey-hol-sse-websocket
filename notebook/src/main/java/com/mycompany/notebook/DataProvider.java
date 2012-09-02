/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.notebook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.media.sse.EventChannel;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;

/**
 *
 * @author martin
 */
class DataProvider {
    private static int lastId = 0;
    private static final HashMap<Integer, Note> notes
            = new HashMap<Integer, Note>();
    private static int eventId = 0;
    
    private static SseBroadcaster broadcaster = new SseBroadcaster();
    
    static synchronized Note getById(int noteId) {
        return notes.get(noteId);
    }

    static synchronized List<Note> allNotes() {
        return new ArrayList(notes.values());
    }
    
    static void registerListener(EventChannel ec) {
        broadcaster.add(ec);
    }
    
    static synchronized int newNote(Note note) {
        Note result = new Note();
        result.id = ++lastId;
        result.name = note.name;
        result.text = note.text;
        notes.put(result.id, result);
        broadcaster.broadcast(new OutboundEvent.Builder()
                .id(String.valueOf(++eventId))
                .name("create")
                .data(Note.class, note)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .build());
        return result.id;
    }

    static synchronized boolean put(Note note) {
        broadcaster.broadcast(new OutboundEvent.Builder()
                .id(String.valueOf(++eventId))
                .name("update")
                .data(Note.class, note)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .build());
        return notes.put(note.id, note) == null;
    }
    
    static synchronized boolean deleteById(int noteId) {
        broadcaster.broadcast(new OutboundEvent.Builder()
                .id(String.valueOf(++eventId))
                .name("delete")
                .data(String.class, String.valueOf(noteId))
                .build());
        return notes.remove(noteId) != null;
    }
}

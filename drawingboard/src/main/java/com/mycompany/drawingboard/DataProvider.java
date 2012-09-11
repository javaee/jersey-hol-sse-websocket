package com.mycompany.drawingboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.media.sse.EventChannel;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;

class DataProvider {
    private static int lastId = 0;
    private static final HashMap<Integer, Drawing> drawings
            = new HashMap<Integer, Drawing>();
    private static int eventId = 0;
    
    private static SseBroadcaster broadcaster = new SseBroadcaster();
    
    static synchronized Drawing getById(int drawingId) {
        return drawings.get(drawingId);
    }

    static synchronized List<Drawing> allDrawings() {
        return new ArrayList(drawings.values());
    }
    
    static void registerListener(EventChannel ec) {
        broadcaster.add(ec);
    }
    
    static synchronized int newDrawing(Drawing drawing) {
        Drawing result = new Drawing();
        result.id = ++lastId;
        result.name = drawing.name;
        result.shapes = drawing.shapes;
        drawings.put(result.id, result);
        broadcaster.broadcast(new OutboundEvent.Builder()
                .id(String.valueOf(++eventId))
                .name("create")
                .data(Drawing.class, drawing)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .build());
        return result.id;
    }

    static synchronized boolean put(Drawing drawing) {
        broadcaster.broadcast(new OutboundEvent.Builder()
                .id(String.valueOf(++eventId))
                .name("update")
                .data(Drawing.class, drawing)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .build());
        return drawings.put(drawing.id, drawing) == null;
    }
    
    static synchronized boolean deleteById(int drawingId) {
        broadcaster.broadcast(new OutboundEvent.Builder()
                .id(String.valueOf(++eventId))
                .name("delete")
                .data(String.class, String.valueOf(drawingId))
                .build());
        return drawings.remove(drawingId) != null;
    }
}

package com.mycompany.drawingboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.websocket.EncodeException;
import javax.net.websocket.Session;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import org.glassfish.jersey.media.sse.EventChannel;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;

class DataProvider {
    private static int lastId = 0;
    private static final HashMap<Integer, Drawing> drawings
            = new HashMap<>();
    private static final MultivaluedHashMap<Integer, Session> webSockets
            = new MultivaluedHashMap<>();
    private static int eventId = 0;
    
    private static SseBroadcaster sseBroadcaster = new SseBroadcaster();
    
    static synchronized Drawing getById(int drawingId) {
        return drawings.get(drawingId);
    }

    static synchronized List<Drawing> allDrawings() {
        return new ArrayList(drawings.values());
    }
    
    static void addEventChannel(EventChannel ec) {
        sseBroadcaster.add(ec);
    }
    
    static synchronized void addWebSocket(int drawingId, Session session) {
        webSockets.add(drawingId, session);
        Drawing drawing = getById(drawingId);
        if (drawing != null && drawing.shapes != null) {
            for (Drawing.Shape shape : drawing.shapes) {
                try {
                    session.getRemote().sendObject(shape);
                } catch (IOException | EncodeException ex) {
                    Logger.getLogger(DataProvider.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    static synchronized void removeWebSocket(int drawingId, Session session) {
        List<Session> sessions = webSockets.get(drawingId);
        if (sessions != null) {
            sessions.remove(session);
        }
    }
    
    static void wsBroadcast(int drawingId, Drawing.Shape shape) {
        synchronized (webSockets) {
            List<Session> sessions = webSockets.get(drawingId);
            if (sessions != null) {
                for (Session session : sessions) {
                    try {
                        session.getRemote().sendObject(shape);
                    } catch (IOException | EncodeException ex) {
                        Logger.getLogger(DataProvider.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    
    static synchronized int newDrawing(Drawing drawing) {
        Drawing result = new Drawing();
        result.id = ++lastId;
        result.name = drawing.name;
        result.shapes = drawing.shapes;
        drawings.put(result.id, result);
        sseBroadcaster.broadcast(new OutboundEvent.Builder()
                .id(String.valueOf(++eventId))
                .name("create")
                .data(Drawing.class, drawing)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .build());
        return result.id;
    }

    static synchronized boolean put(Drawing drawing) {
        sseBroadcaster.broadcast(new OutboundEvent.Builder()
                .id(String.valueOf(++eventId))
                .name("update")
                .data(Drawing.class, drawing)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .build());
        return drawings.put(drawing.id, drawing) == null;
    }
    
    static synchronized boolean deleteById(int drawingId) {
        sseBroadcaster.broadcast(new OutboundEvent.Builder()
                .id(String.valueOf(++eventId))
                .name("delete")
                .data(String.class, String.valueOf(drawingId))
                .build());
        return drawings.remove(drawingId) != null;
    }
    
    static synchronized void newShape(int drawingId, Drawing.Shape shape) {
        Drawing drawing = getById(drawingId);
        if (drawing != null) {
            if (drawing.shapes == null) {
                drawing.shapes = new ArrayList<>();
            }
            drawing.shapes.add(shape);
            wsBroadcast(drawingId, shape);
        }
    }
    
    static synchronized void clearShapes(int drawingId) {
        getById(drawingId).shapes.clear();
        wsBroadcast(drawingId, Drawing.Shape.NULL);
    }
}

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

/**
 * Simple in-memory data storage for the application.
 */
class DataProvider {
    /** ID of the last created drawing. */
    private static int lastId = 0;
    
    /** Map that stores drawings by ID. */
    private static final HashMap<Integer, Drawing> drawings
            = new HashMap<>();
    
    /** Broadcaster for server-sent events. */
    private static SseBroadcaster sseBroadcaster = new SseBroadcaster();

    /** Map that stores web socket sessions corresponding to a given drawing ID. */
    private static final MultivaluedHashMap<Integer, Session> webSockets
            = new MultivaluedHashMap<>();
    
    /**
     * Retrieves a drawing by ID.
     * @param drawingId ID of the drawing to be retrieved.
     * @return Drawing with the corresponding ID.
     */
    static synchronized Drawing getDrawing(int drawingId) {
        return drawings.get(drawingId);
    }

    /**
     * Retrieves all existing drawings.
     * @return List of all drawings.
     */
    static synchronized List<Drawing> getAllDrawings() {
        return new ArrayList(drawings.values());
    }
    
    /**
     * Creates a new drawing based on the supplied drawing object.
     * @param drawing Drawing object containing property values for the new drawing.
     * @return ID of the newly created drawing.
     */
    static synchronized int createDrawing(Drawing drawing) {
        Drawing result = new Drawing();
        result.id = ++lastId;
        result.name = drawing.name;
        result.shapes = drawing.shapes;
        drawings.put(result.id, result);
        sseBroadcaster.broadcast(new OutboundEvent.Builder()
                .name("create")
                .data(Drawing.class, result)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .build());
        return result.id;
    }

    /**
     * Delete a drawing with a given ID.
     * @param drawingId ID of the drawing to be deleted.
     * @return {@code true} if the drawing was deleted, {@code false} if there
     *         was no such drawing.
     */
    static synchronized boolean deleteDrawing(int drawingId) {
        sseBroadcaster.broadcast(new OutboundEvent.Builder()
                .name("delete")
                .data(String.class, String.valueOf(drawingId))
                .build());
        return drawings.remove(drawingId) != null;
    }
    
    /**
     * Add a new shape to the drawing.
     * @param drawingId ID of the drawing the shape should be added to.
     * @param shape Shape to be added to the drawing.
     * @return {@code true} if the shape was added, {@code false} if no such
     *         drawing was found.
     */
    static synchronized boolean addShape(int drawingId, Drawing.Shape shape) {
        Drawing drawing = getDrawing(drawingId);
        if (drawing != null) {
            if (drawing.shapes == null) {
                drawing.shapes = new ArrayList<>();
            }
            drawing.shapes.add(shape);
            wsBroadcast(drawingId, shape);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Delete all shapes from the drawing.
     * @param drawingId ID of the drawing that should be cleared.
     * @return {@code true} if the drawing was cleared, {@code false} if no such
     *         drawing was found.
     */
    static synchronized boolean clearShapes(int drawingId) {
        Drawing drawing = getDrawing(drawingId);
        if (drawing != null) {
            drawing.shapes.clear();
            wsBroadcast(drawingId, ShapeCoding.SHAPE_CLEAR_ALL);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Registers a new channel for sending events. An event channel corresponds
     * to a client (browser) event source connection.
     * @param ec Event channel to be registered for sending events.
     */
    static void addEventChannel(EventChannel ec) {
        sseBroadcaster.add(ec);
    }
    
    /**
     * Registers a new web socket session and associates it with a drawing ID.
     * This method should be called when a client opens a web socket connection
     * to a particular drawing URI.
     * @param drawingId Drawing ID to associate the web socket session with.
     * @param session New web socket session to be registered.
     */
    static synchronized void addWebSocket(int drawingId, Session session) {
        webSockets.add(drawingId, session);
        Drawing drawing = getDrawing(drawingId);
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
    
    /**
     * Removes the existing web socket session associated with a drawing ID.
     * This method should be called when a client closes the web socket connection
     * to a particular drawing URI.
     * @param drawingId ID of the drawing the web socket session is associated with.
     * @param session Web socket session to be removed.
     */
    static synchronized void removeWebSocket(int drawingId, Session session) {
        List<Session> sessions = webSockets.get(drawingId);
        if (sessions != null) {
            sessions.remove(session);
        }
    }
    
    /**
     * Broadcasts the newly added shape to all web sockets associated with the
     * affected drawing.
     * @param drawingId ID of the affected drawing.
     * @param shape Shape that was added to the drawing or {@link ShapeCoding#SHAPE_CLEAR_ALL}
     *              if the drawing was cleared (i.e. all shapes were deleted).
     */
    private static void wsBroadcast(int drawingId, Drawing.Shape shape) {
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
}

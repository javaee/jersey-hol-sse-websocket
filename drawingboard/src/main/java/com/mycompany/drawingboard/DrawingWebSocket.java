package com.mycompany.drawingboard;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.websocket.CloseReason;
import javax.net.websocket.Session;
import javax.net.websocket.annotations.WebSocketClose;
import javax.net.websocket.annotations.WebSocketEndpoint;
import javax.net.websocket.annotations.WebSocketMessage;
import javax.net.websocket.annotations.WebSocketOpen;

/**
 * Class handling web socket connections at "/websockets/*" path.
 */
@WebSocketEndpoint(
        decoders = ShapeCoding.class,
        encoders = ShapeCoding.class,
        path = "/websockets/"
)
public class DrawingWebSocket {
    private static final Pattern URI_PATTERN = Pattern.compile("(?:.*)/websockets/([0-9]+)");
    private static final ConcurrentHashMap<Session, Integer> sessionToId = new ConcurrentHashMap<>();

    /**
     * Method called by the web socket runtime when a new web socket opens.
     * @param session Session associated with the new web socket connection.
     */
    @WebSocketOpen
    public void onOpen(Session session) {
        Matcher matcher = URI_PATTERN.matcher(session.getRequestURI().toString());
        if (!matcher.matches()) {
            try {
                session.close(new CloseReason(CloseReason.Code.CANNOT_ACCEPT, "Not found."));
            } catch (IOException ex) {
                Logger.getLogger(DrawingWebSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            int drawingId = Integer.parseInt(matcher.group(1));
            sessionToId.put(session, drawingId);
            DataProvider.addWebSocket(drawingId, session);
        }
    }
    
    /**
     * Method called by the web socket runtime when the web socket connection
     * closes.
     * @param session Session associated with the web socket connection being closed.
     */
    @WebSocketClose
    public void onClose(Session session){
        int drawingId = sessionToId.remove(session);
        DataProvider.removeWebSocket(drawingId, session);
    }

    /**
     * Handler for the incoming web socket messages.
     * @param shape Body of the message (in our case it is always decoded as
     *              an instance of Drawing.Shape).
     * @param session Session associated with the web socket connection receiving
     *                the message.
     */
    @WebSocketMessage
    public void shapeCreated(Drawing.Shape shape, Session session) {
        int drawingId = sessionToId.get(session);
        DataProvider.addShape(drawingId, shape);
    }
}

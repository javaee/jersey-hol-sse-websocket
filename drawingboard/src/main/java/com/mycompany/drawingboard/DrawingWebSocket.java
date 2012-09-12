package com.mycompany.drawingboard;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.websocket.CloseReason;
import javax.net.websocket.EncodeException;
import javax.net.websocket.Session;
import javax.net.websocket.annotations.WebSocketClose;
import javax.net.websocket.annotations.WebSocketEndpoint;
import javax.net.websocket.annotations.WebSocketMessage;
import javax.net.websocket.annotations.WebSocketOpen;

@WebSocketEndpoint(
        decoders = ShapeCoding.class,
        encoders = ShapeCoding.class,
        path = "/drawings/websockets/"
)
public class DrawingWebSocket {
    private static final Pattern URI_PATTERN = Pattern.compile("(?:.*)/drawings/websockets/([0-9]+)");
    private static final ConcurrentHashMap<Session, Integer> sessionToId = new ConcurrentHashMap<>();

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

    @WebSocketClose
    public void onClose(Session session){
        int drawingId = sessionToId.get(session);
        DataProvider.removeWebSocket(drawingId, session);
    }

    @WebSocketMessage
    public void shapeCreated(Drawing.Shape shape, Session session) throws IOException, EncodeException {
        int drawingId = sessionToId.get(session);
        if (shape == Drawing.Shape.NULL) {
            DataProvider.clearShapes(drawingId);
        } else {
            DataProvider.newShape(drawingId, shape);
        }
    }
}

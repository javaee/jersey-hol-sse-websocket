package com.mycompany.drawingboard;

import javax.websocket.Session;
import javax.websocket.OnClose;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;

/**
 * Class handling web socket connections at "/websockets/{id}" path.
 */
@ServerEndpoint(
        value = "/websockets/{id}",
        decoders = ShapeCoding.class,
        encoders = ShapeCoding.class)
public class DrawingWebSocket {

    /**
     * Method called by the web socket runtime when a new web socket opens.
     *
     * @param session Session associated with the new web socket connection.
     */
    @OnOpen
    public void onOpen(@PathParam("id") Integer drawingId, Session session) {
        DataProvider.addWebSocket(drawingId, session);
    }

    /**
     * Method called by the web socket runtime when the web socket connection
     * closes.
     *
     * @param session Session associated with the web socket connection being
     * closed.
     */
    @OnClose
    public void onClose(@PathParam("id") Integer drawingId, Session session) {
        DataProvider.removeWebSocket(drawingId, session);
    }

    /**
     * Handler for the incoming web socket messages.
     *
     * @param shape Body of the message (in our case it is always decoded as an
     * instance of Drawing.Shape).
     * @param session Session associated with the web socket connection
     * receiving the message.
     */
    @OnMessage
    public void shapeCreated(@PathParam("id") Integer drawingId, Drawing.Shape shape, Session session) {
        DataProvider.addShape(drawingId, shape);
    }
}

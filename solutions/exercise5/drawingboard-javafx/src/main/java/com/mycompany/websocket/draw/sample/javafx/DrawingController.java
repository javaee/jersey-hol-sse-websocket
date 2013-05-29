/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.websocket.draw.sample.javafx;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import netscape.javascript.JSObject;

/**
 * FXML Controller class
 *
 * @author pdos
 */
public class DrawingController implements Initializable {

    @FXML
    private WebView webview;
    private WebEngine engine;
    private DrawingsEventSource eventSource;
    private WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    private WSClient wsClient;
    private HashMap<String, WSClient> webSocketSessions = new HashMap<>();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        engine = webview.getEngine();
        setupEngine();
        engine.load("http://localhost:8080/drawingboard/");

    }

    private void setupEngine() {
        engine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> ov,
                    Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject global =
                            (JSObject) engine.executeScript("window");
                    global.setMember("webSocketOpen", new WebSocketOpen());
                    global.setMember("webSocketClose", new WebSocketClose());
                    global.setMember("webSocketSend", new WebSocketSend());
                    initSSE();
                }
            }
        });
    }

    private void initSSE() {
        Client client = ClientBuilder.newClient();
        // create a web target pointing to the drawings resource
        WebTarget t = client.target("http://localhost:8080/drawingboard/api/drawings");
        // start listening to SSE
        eventSource = new DrawingsEventSource(t.path("events"), engine);
    }

    public class WebSocketSend {

        public void send(String drawing) {
            try {
                System.out.println("sending drawing: " + drawing);
                wsClient.getSession().getBasicRemote().sendText(drawing);
            } catch (IOException ex) {
                Logger.getLogger(DrawingController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public class WebSocketOpen {

        public void open(String baseURL, String drawingId) {
            try {
                System.out.println("Setting WebSocket to " + baseURL + drawingId);
                wsClient = new WSClient(webview);
                URI clientURI = new URI(baseURL + drawingId);
                container.connectToServer(wsClient, clientURI);
                webSocketSessions.put(drawingId, wsClient);
            } catch (URISyntaxException | DeploymentException | IOException ex) {
                Logger.getLogger(DrawingController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public class WebSocketClose {

        public void close(String drawingId) {
            System.out.println("Closing socket for drawing " + drawingId);
            WSClient client = (WSClient) webSocketSessions.get(drawingId);
            if (client == null) return;
            try {
                client.getSession().close();
            } catch (IOException ex) {
                Logger.getLogger(DrawingController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

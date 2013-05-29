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
import java.util.List;
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
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import netscape.javascript.JSObject;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.w3c.dom.Document;

/**
 * FXML Controller class
 *
 * @author pdos
 */
public class DrawingController implements Initializable {

    @FXML
    private WebView webview;
    private WebEngine engine;

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
    }

}

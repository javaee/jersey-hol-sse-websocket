package com.mycompany.websocket.draw.sample.javafx;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import javafx.application.Platform;
import javafx.scene.web.WebView;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

/**
 *
 * @author pdos
 */
@ClientEndpoint
public class WSClient {

    private Session session;
    private WebView webview;

    public WebView getWebview() {
        return webview;
    }

    public Session getSession() {
        return session;
    }

    public WSClient(WebView webview) {
        this.webview = webview;
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connection had opened.");
    }

    @OnMessage
    public void onMessage(String message) {
        final String js_script =
            "var elem = angular.element(document.getElementsByClassName(\"form-horizontal\"));"
            + "var sc = elem.scope();"
            + "sc.drawShape(eval(" + message + "));";
        System.out.println("script to execute: " + js_script);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                webview.getEngine().executeScript(js_script);
            }
        });
    }

    @OnClose
    public void closeConnection(Session session) {
        System.out.println("Connection had closed.");
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.websocket.draw.sample.javafx;

import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;

/**
 *
 * @author pdos
 */
class DrawingsEventSource extends EventSource {

    private WebEngine engine;
    private String js_script =
            "var elem = angular.element(document.getElementsByClassName(\"table\"));"
            + "var sc = elem.scope();"
            + "var svc = elem.injector().get('DrawingService');"
            + "sc.drawings = svc.query();"
            + "sc.$apply();";

    public DrawingsEventSource(WebTarget target, WebEngine engine) throws NullPointerException {
        super(target);
        this.engine = engine;
    }

    @Override
    public void onEvent(InboundEvent inboundEvent) {
        try {
            System.out.println("Event "
                    + inboundEvent.getName() + ": "
                    + inboundEvent.getData());
            System.out.println("script to execute: " + js_script);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    engine.executeScript(js_script);
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

package com.mycompany.drawingboardclient;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientFactory;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.moxy.json.MoxyJsonBinder;

public class Main {
    public static void main(String[] args) throws IOException {
        Client client = ClientFactory.newClient(
                new ClientConfig().binders(new MoxyJsonBinder()));
        WebTarget appRoot = client.target("http://localhost:8080/drawingboard-api");
        
        List<Drawing> drawings = appRoot.path("drawings")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Drawing>>() {});
        
        System.out.println(drawings);
        
        EventSource eventSource = new EventSource(appRoot.path("drawings/events")) {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                try {
                    System.out.println("Event " + inboundEvent.getName() + ": "
                        + inboundEvent.getData());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
        
        System.in.read();
        
        eventSource.close();
    }
}

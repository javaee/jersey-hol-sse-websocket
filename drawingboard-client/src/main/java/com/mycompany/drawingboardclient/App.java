package com.mycompany.drawingboardclient;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientFactory;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.moxy.json.MoxyJsonBinder;

public class App {
    public static void main(String[] args) throws IOException {
        // create a new client with MOXy JSON binding support enabled
        Client client = ClientFactory.newClient(
                new ClientConfig().binders(new MoxyJsonBinder()));
        
        // create a web target pointing to the drawings resource
        WebTarget t = client.
                target("http://localhost:8080/drawingboard/api/drawings");
        
        // retrieve the list of drawings and print it out
        List<Drawing> drawings = t.request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Drawing>>() {});
        System.out.println(drawings);
        
        // start listening to SSE
        EventSource events = new EventSource(t.path("events")) {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                try {
                    System.out.println("Event " + 
                            inboundEvent.getName() + ": " +
                            inboundEvent.getData());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
        
        System.out.println("Listening to the SSE. Press Enter to stop.");
        System.in.read();
        
        // close and exit
        events.close();
        System.exit(0);
    }
}

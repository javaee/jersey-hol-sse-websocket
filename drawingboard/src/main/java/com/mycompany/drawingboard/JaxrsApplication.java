package com.mycompany.drawingboard;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.media.sse.OutboundEventWriter;
import org.glassfish.jersey.moxy.json.MoxyJsonBinder;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("api")
public class JaxrsApplication extends ResourceConfig {
    public JaxrsApplication() {
        packages("com.mycompany.drawingboard")
        // add message body writer for SSE
        .addClasses(OutboundEventWriter.class)
        // add support for JSON via MOXy
        .addBinders(new MoxyJsonBinder());        
    }
}

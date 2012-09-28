package com.mycompany.drawingboard;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.media.sse.OutboundEventWriter;
import org.glassfish.jersey.moxy.json.MoxyJsonBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * JAX-RS application class.
 */
@ApplicationPath("api")
public class JaxrsApplication extends ResourceConfig {
    public JaxrsApplication() {
        // consists of a single resource, and a writer for SSE
        super(DrawingsResource.class, OutboundEventWriter.class);
        // add MOXy binders for JSON String<->Java conversion
        addBinders(new MoxyJsonBinder());
    }
}

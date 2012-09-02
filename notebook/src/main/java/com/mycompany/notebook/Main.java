package com.mycompany.notebook;

import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.ext.RuntimeDelegate;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.internal.ProcessingException;
import org.glassfish.jersey.media.sse.OutboundEventWriter;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.moxy.json.MoxyJsonBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Main class.
 *
 */
public class Main {
    public static final String APP_PATH = "/notebook/";
    public static final String API_PATH = "/notebook-api/";
    public static final String WEB_ROOT = "/webroot";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        final HttpServer server = new HttpServer();
        final NetworkListener listener = new NetworkListener("grizzly", "localhost", 8080);

        server.addListener(listener);

        // Map the path to the processor.
        final ServerConfiguration config = server.getServerConfiguration();

        config.addHttpHandler(createJerseyHandler(), API_PATH);
        config.addHttpHandler(new StaticContentHandler(), APP_PATH);

        try {
            // Start the server.
            server.start();
        } catch (IOException ex) {
            throw new ProcessingException("IOException thrown when trying to start grizzly server", ex);
        }
        
        return server;
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started. The resources are"
                + "available under http://localhost:8080%s\nHit enter to stop it..."
                , API_PATH));
        System.in.read();
        server.stop();
    }
    
    private static HttpHandler createJerseyHandler() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.mycompany.notebook package
        final ResourceConfig rc = new ResourceConfig()
                .packages("com.mycompany.notebook")
                .addClasses(OutboundEventWriter.class)
                .addBinders(new MoxyJsonBinder());

       
        return RuntimeDelegate.getInstance().createEndpoint(rc, GrizzlyHttpContainer.class);
    }

    private static class StaticContentHandler extends HttpHandler {
        private static final String[] ALLOWED_EXTENSIONS = {".html", ".js", 
            ".css", ".png", ".ico"};
        
        @Override
        public void service(Request request, Response response) throws Exception {
            String uri = request.getRequestURI();
            if (uri.contains("..") || !checkExtension(uri)) {
                response.sendError(HttpStatus.NOT_FOUND_404.getStatusCode());
                return;
            }

            final String resourcesContextPath = request.getContextPath();
            if (resourcesContextPath != null && !resourcesContextPath.isEmpty()) {
                if (!uri.startsWith(resourcesContextPath)) {
                    response.sendError(HttpStatus.NOT_FOUND_404.getStatusCode());
                    return;
                }

                uri = uri.substring(resourcesContextPath.length());
            }
            
            InputStream fileStream = Main.class.getResourceAsStream(WEB_ROOT + uri);
            if (fileStream == null) {
                response.sendError(HttpStatus.NOT_FOUND_404.getStatusCode());
            } else {
                response.setStatus(HttpStatus.OK_200);
                ReaderWriter.writeTo(fileStream, response.getOutputStream());
            }
        }
        
        private boolean checkExtension(String uri) {
            for (String extension : ALLOWED_EXTENSIONS) {
                if (uri.endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }
}
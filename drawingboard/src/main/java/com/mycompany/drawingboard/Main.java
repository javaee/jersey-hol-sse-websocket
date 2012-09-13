package com.mycompany.drawingboard;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import javax.ws.rs.ext.RuntimeDelegate;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.internal.ProcessingException;
import org.glassfish.jersey.media.sse.OutboundEventWriter;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.moxy.json.MoxyJsonBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.tyrus.platform.BeanServer;
import org.glassfish.tyrus.spi.grizzlyprovider.GrizzlyEngine;

public class Main {
    public static final String APP_PATH = "/drawingboard/";
    public static final String API_PATH = "/drawingboard-api/";
    public static final String WEB_ROOT = "/webroot";
    public static final int PORT = 8080;

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer(String webrootPath) {
        final HttpServer server = new HttpServer();
        final NetworkListener listener = new NetworkListener("grizzly", "localhost", PORT);
        listener.registerAddOn(new WebSocketAddOn());
        
        server.addListener(listener);

        // Map the path to the processor.
        final ServerConfiguration config = server.getServerConfiguration();

        config.addHttpHandler(createJerseyHandler(), API_PATH);
        config.addHttpHandler(new StaticContentHandler(webrootPath), APP_PATH);

        try {
            BeanServer beanServer = new BeanServer(GrizzlyEngine.class.getName());
            beanServer.initWebSocketServer(API_PATH, PORT,
                    Collections.<Class<?>>singleton(DrawingWebSocket.class));
            // Start the server.
            server.start();
        } catch (Exception ex) {
            throw new ProcessingException("Exception thrown when trying to start grizzly server", ex);
        }
        
        return server;
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer(args.length >= 1 ? args[0] : null);
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
                .packages("com.mycompany.drawingboard")
                .addClasses(OutboundEventWriter.class)
                .addBinders(new MoxyJsonBinder());

       
        return RuntimeDelegate.getInstance().createEndpoint(rc, GrizzlyHttpContainer.class);
    }

    private static class StaticContentHandler extends HttpHandler {
        private static final HashMap<String, String> EXTENSION_TO_MEDIA_TYPE;
        
        static {
            EXTENSION_TO_MEDIA_TYPE = new HashMap<String, String>();
            
            EXTENSION_TO_MEDIA_TYPE.put("html", "text/html");
            EXTENSION_TO_MEDIA_TYPE.put("js", "application/javascript");
            EXTENSION_TO_MEDIA_TYPE.put("css", "text/css");
            EXTENSION_TO_MEDIA_TYPE.put("png", "image/png");
            EXTENSION_TO_MEDIA_TYPE.put("ico", "image/png");
        }
        
        private final String webrootPath;
        
        StaticContentHandler(String webrootPath) {
            this.webrootPath = webrootPath;
        }
        
        @Override
        public void service(Request request, Response response) throws Exception {
            String uri = request.getRequestURI();
            
            int pos = uri.lastIndexOf('.');
            String extension = uri.substring(pos + 1);
            String mediaType = EXTENSION_TO_MEDIA_TYPE.get(extension);
            
            if (uri.contains("..") || mediaType == null) {
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
            
            InputStream fileStream;
            
            try {
                fileStream = webrootPath == null ?
                        Main.class.getResourceAsStream(WEB_ROOT + uri) :
                        new FileInputStream(webrootPath + uri);
            } catch (IOException e) {
                fileStream = null;
            }
            
            if (fileStream == null) {
                response.sendError(HttpStatus.NOT_FOUND_404.getStatusCode());
            } else {
                response.setStatus(HttpStatus.OK_200);
                response.setContentType(mediaType);
                ReaderWriter.writeTo(fileStream, response.getOutputStream());
            }
        }
    }
}
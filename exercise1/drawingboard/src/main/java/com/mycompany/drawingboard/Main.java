package com.mycompany.drawingboard;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.internal.ProcessingException;
import org.glassfish.jersey.message.internal.ReaderWriter;

/**
 * Main application class. Starts the embedded Grizzly HTTP server.
 */
public class Main {
    public static final String APP_PATH = "/drawingboard/";
    public static final String API_PATH = "/drawingboard-api/";
    public static final String WEB_ROOT = "/webroot";
    public static final int PORT = 8080;

    /**
     * Starts Grizzly HTTP server.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer(String webrootPath) {
        final HttpServer server = new HttpServer();
        final NetworkListener listener = new NetworkListener("grizzly", "localhost", PORT);
        
        server.addListener(listener);

        final ServerConfiguration config = server.getServerConfiguration();
        // add handler for serving static content
        config.addHttpHandler(new StaticContentHandler(webrootPath), APP_PATH);

        try {
            // Start the server.
            server.start();
        } catch (Exception ex) {
            throw new ProcessingException("Exception thrown when trying to start grizzly server", ex);
        }
        
        return server;
    }

    /**
     * Main method.
     * @param args First command line argument can be used to point to the location
     *             of static content (by default the static content is read from
     *             webroot directory of the application jar file.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer(args.length >= 1 ? args[0] : null);
        System.out.println(String.format("DrawingBoard started."
                + "Access it at http://localhost:%s%sindex.html\n"
                + "Hit enter to stop it..."
                , PORT, APP_PATH));
        System.in.read();
        server.stop();
    }
    
    /**
     * Simple HttpHandler for serving static content included in webroot
     * directory of this application.
     */
    private static class StaticContentHandler extends HttpHandler {
        private static final HashMap<String, String> EXTENSION_TO_MEDIA_TYPE;
        
        static {
            EXTENSION_TO_MEDIA_TYPE = new HashMap<>();
            
            EXTENSION_TO_MEDIA_TYPE.put("html", "text/html");
            EXTENSION_TO_MEDIA_TYPE.put("js", "application/javascript");
            EXTENSION_TO_MEDIA_TYPE.put("css", "text/css");
            EXTENSION_TO_MEDIA_TYPE.put("png", "image/png");
            EXTENSION_TO_MEDIA_TYPE.put("ico", "image/png");
        }
        
        private final String webrootPath;
        
        StaticContentHandler(String webrootPath) {
            this.webrootPath = webrootPath;
            if (webrootPath != null) {
                System.out.println("Reading static content from the following"
                        + " directory: " + webrootPath);
            }
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
            
            InputStream fileStream = null;
            
            if (webrootPath != null) {
                try {
                    fileStream = new FileInputStream(webrootPath + uri);
                } catch (IOException e) {
                    fileStream = null;
                }
            }
            
            if (fileStream == null) {
                fileStream = getClass().getResourceAsStream(WEB_ROOT + uri);
            }
            
            if (fileStream == null) {
                response.sendError(HttpStatus.NOT_FOUND_404.getStatusCode());
            } else {
                response.setStatus(HttpStatus.OK_200);
                response.setContentType(mediaType);
                try (InputStream is = fileStream) {
                    ReaderWriter.writeTo(is, response.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
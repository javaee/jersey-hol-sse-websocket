package com.mycompany.drawingboard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import javax.net.websocket.DecodeException;
import javax.net.websocket.Decoder;
import javax.net.websocket.EncodeException;
import javax.net.websocket.Encoder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.eclipse.persistence.jaxb.JAXBContextFactory;

/**
 * Encoder and decoder that de/en-codes web socket messages into/from Shape objects.
 */
public class ShapeCoding implements Decoder.Text<Drawing.Shape>, Encoder.Text<Drawing.Shape> {
    /** 
     * Special instance of Shape to indicate all shapes for a given drawing
     * should be cleared.
     */
    public static final Drawing.Shape SHAPE_CLEAR_ALL = new Drawing.Shape();

    private static final Unmarshaller UNMARSHALLER;
    private static final Marshaller MARSHALLER;
    
    static {
        try {
            JAXBContext jaxbContext = JAXBContextFactory.createContext(
                new Class[] {
                    Drawing.Shape.class,
                }, Collections.emptyMap());
            UNMARSHALLER = jaxbContext.createUnmarshaller();
            UNMARSHALLER.setProperty("eclipselink.media-type", "application/json");
            UNMARSHALLER.setProperty("eclipselink.json.include-root", false);
            MARSHALLER = jaxbContext.createMarshaller();
            MARSHALLER.setProperty("eclipselink.media-type", "application/json");
            MARSHALLER.setProperty("eclipselink.json.include-root", false);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public Drawing.Shape decode(String s) throws DecodeException {
        if ("clear".equals(s)) {
            return SHAPE_CLEAR_ALL;
        }
        
        try {
            return UNMARSHALLER.unmarshal(
                    new StreamSource(new ByteArrayInputStream(s.getBytes())),
                    Drawing.Shape.class).getValue();
        } catch (JAXBException ex) {
            throw new DecodeException();
        }
    }

    @Override
    public boolean willDecode(String s) {
        // we can always return true, as this decoder can work with any messages
        // used by this application
        return true;
    }

    @Override
    public String encode(Drawing.Shape object) throws EncodeException {
        if (object == SHAPE_CLEAR_ALL) {
            return "clear";
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            MARSHALLER.marshal(object, baos);
        } catch (JAXBException ex) {
            throw new EncodeException(ex.getMessage(), ex);
        }
        
        return new String(baos.toByteArray());
    }
}

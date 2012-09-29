package com.mycompany.drawingboard;

import java.io.StringReader;
import java.io.StringWriter;
import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.stream.JsonGenerator;
import javax.net.websocket.DecodeException;
import javax.net.websocket.Decoder;
import javax.net.websocket.EncodeException;
import javax.net.websocket.Encoder;

/**
 * Encoder and decoder that de/en-codes web socket messages into/from Shape objects.
 */
public class ShapeCoding implements Decoder.Text<Drawing.Shape>, Encoder.Text<Drawing.Shape> {
    @Override
    public Drawing.Shape decode(String s) throws DecodeException {
        // temporary workaround for a web socket implementation issue
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        
        Drawing.Shape shape = new Drawing.Shape();
        
        try (JsonReader reader = new JsonReader(new StringReader(s))) {
            JsonObject object = reader.readObject();
            shape.x = object.getValue("x", JsonNumber.class).getIntValue();
            shape.y = object.getValue("y", JsonNumber.class).getIntValue();
            shape.type = Drawing.ShapeType.valueOf(
                    object.getValue("type", JsonString.class).getValue());
            shape.color = Drawing.ShapeColor.valueOf(
                    object.getValue("color", JsonString.class).getValue());
        }
        
        return shape;
    }

    @Override
    public boolean willDecode(String s) {
        // we can always return true, as this decoder can work with any messages
        // used by this application
        return true;
    }

    @Override
    public String encode(Drawing.Shape object) throws EncodeException {
        // temporary workaround for a web socket implementation issue
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        StringWriter result = new StringWriter();
        
        try (JsonGenerator gen = Json.createGenerator(result)) {
            gen.beginObject()
                    .add("x", object.x)
                    .add("y", object.y)
                    .add("type", object.type.toString())
                    .add("color", object.color.toString())
                    .endObject();
        }

        return result.toString();
    }
}

package com.mycompany.drawingboard;

import java.io.StringReader;
import java.io.StringWriter;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * Encoder and decoder that de/en-codes web socket messages into/from Shape
 * objects.
 */
public class ShapeCoding implements Decoder.Text<Drawing.Shape>, Encoder.Text<Drawing.Shape> {

    @Override
    public Drawing.Shape decode(String s) throws DecodeException {
        // temporary workaround for a web socket implementation issue
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        Drawing.Shape shape = new Drawing.Shape();

        try (JsonReader reader = Json.createReader(new StringReader(s))) {
            JsonObject object = reader.readObject();
            shape.x = object.getInt("x");
            shape.y = object.getInt("y");
            shape.type = Drawing.ShapeType.valueOf(
                    object.getString("type"));
            shape.color = Drawing.ShapeColor.valueOf(
                    object.getString("color"));
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
            gen.writeStartObject()
                    .write("x", object.x)
                    .write("y", object.y)
                    .write("type", object.type.toString())
                    .write("color", object.color.toString())
                    .writeEnd();
        }

        return result.toString();
    }

    @Override
    public void init(EndpointConfig ec) {
    }

    @Override
    public void destroy() {
    }
}

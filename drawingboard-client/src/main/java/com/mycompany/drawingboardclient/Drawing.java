package com.mycompany.drawingboardclient;

import java.util.List;

/**
 * POJO representing a drawing.
 */
public class Drawing {
    /** Drawing ID. */
    public int id;
    
    /** Drawing name. */
    public String name;
    
    /** 
     * List of shapes the drawing consists of (or {@code null} if the drawing
     * is empty.
     */
    public List<Shape> shapes;
    
    /**
     * POJO representing a shape.
     */
    public static class Shape {
        /** Type of the shape. */
        public ShapeType type;
        
        /** Shape coordinates. */
        public int x, y;
        
        /** Shape color. */
        public ShapeColor color;

        @Override
        public String toString() {
            return "Shape(" + x + ", " + y + ", " + type + ", " + color + ")";
        }
    }
    
    /**
     * Shape types.
     */
    public static enum ShapeType {
        BIG_CIRCLE, 
        SMALL_CIRCLE, 
        BIG_SQUARE, 
        SMALL_SQUARE,
    }
    
    /**
     * Shape colors.
     */
    public static enum ShapeColor {
        RED,
        GREEN,
        BLUE,
        YELLOW,
    }

    @Override
    public String toString() {
        return "Drawing(" + name + ", " + shapes + ")";
    }
}

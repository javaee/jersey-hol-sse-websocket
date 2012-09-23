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
    }
    
    /**
     * Enum for shape types.
     */
    public static enum ShapeType {
        BIG_CIRCLE, 
        SMALL_CIRCLE, 
        BIG_SQUARE, 
        SMALL_SQUARE,
    }
    
    /**
     * Enum or shape colors.
     */
    public static enum ShapeColor {
        RED,
        GREEN,
        BLUE,
        YELLOW,
    }
}

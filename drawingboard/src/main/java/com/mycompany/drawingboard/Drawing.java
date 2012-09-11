package com.mycompany.drawingboard;

import java.util.List;

public class Drawing {
    public int id;
    public String name;
    public List<Shape> shapes;
    
    public static class Shape {
        public ShapeType type;
        public int x, y;
        public ShapeColor color;
    }
    
    public static enum ShapeType {
        BIG_CIRCLE, 
        SMALL_CIRCLE, 
        BIG_SQUARE, 
        SMALL_SQUARE,
    }
    
    public static enum ShapeColor {
        RED,
        GREEN,
        BLUE,
        YELLOW,
    }
}

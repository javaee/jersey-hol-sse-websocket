package com.mycompany.drawingboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Simple in-memory data storage for the application.
 */
class DataProvider {
    /** ID of the last created drawing. */
    private static int lastId = 0;
    
    /** Map that stores drawings by ID. */
    private static final HashMap<Integer, Drawing> drawings
            = new HashMap<>();
    
    /**
     * Retrieves a drawing by ID.
     * @param drawingId ID of the drawing to be retrieved.
     * @return Drawing with the corresponding ID.
     */
    static synchronized Drawing getDrawing(int drawingId) {
        return drawings.get(drawingId);
    }

    /**
     * Retrieves all existing drawings.
     * @return List of all drawings.
     */
    static synchronized List<Drawing> getAllDrawings() {
        return new ArrayList(drawings.values());
    }
    
    /**
     * Creates a new drawing based on the supplied drawing object.
     * @param drawing Drawing object containing property values for the new drawing.
     * @return ID of the newly created drawing.
     */
    static synchronized int createDrawing(Drawing drawing) {
        Drawing result = new Drawing();
        result.id = ++lastId;
        result.name = drawing.name;
        result.shapes = drawing.shapes;
        drawings.put(result.id, result);
        return result.id;
    }

    /**
     * Delete a drawing with a given ID.
     * @param drawingId ID of the drawing to be deleted.
     * @return {@code true} if the drawing was deleted, {@code false} if there
     *         was no such drawing.
     */
    static synchronized boolean deleteDrawing(int drawingId) {
        return drawings.remove(drawingId) != null;
    }
    
    /**
     * Add a new shape to the drawing.
     * @param drawingId ID of the drawing the shape should be added to.
     * @param shape Shape to be added to the drawing.
     * @return {@code true} if the shape was added, {@code false} if no such
     *         drawing was found.
     */
    static synchronized boolean addShape(int drawingId, Drawing.Shape shape) {
        Drawing drawing = getDrawing(drawingId);
        if (drawing != null) {
            if (drawing.shapes == null) {
                drawing.shapes = new ArrayList<>();
            }
            drawing.shapes.add(shape);
            return true;
        } else {
            return false;
        }
    }
}

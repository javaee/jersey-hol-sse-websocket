/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.notebook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author martin
 */
class DataProvider {
    private static int lastId = 0;
    private static final HashMap<Integer, Note> notes
            = new HashMap<Integer, Note>();
    
    static synchronized Note getById(int noteId) {
        return notes.get(noteId);
    }

    static synchronized List<Note> allNotes() {
        return new ArrayList(notes.values());
    }
    
    static synchronized int newNote(Note note) {
        Note result = new Note();
        result.id = ++lastId;
        result.name = note.name;
        result.text = note.text;
        notes.put(result.id, result);
        return result.id;
    }

    static synchronized boolean put(Note note) {
        return notes.put(note.id, note) == null;
    }
    
    static synchronized boolean deleteById(int noteId) {
        return notes.remove(noteId) != null;
    }
}

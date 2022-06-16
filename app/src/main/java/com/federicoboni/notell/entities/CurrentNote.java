package com.federicoboni.notell.entities;

import com.federicoboni.notell.database.entities.Note;

import java.util.Date;

public class CurrentNote {
    private static final String EMPTY_STRING = "";
    private static CurrentNote currentNote;
    private Note note;

    private CurrentNote(Note note) {
        this.note = note;
    }

    public static synchronized CurrentNote getInstance() {
        if (currentNote == null) {
            currentNote = new CurrentNote(new Note(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, new Date().getTime(), null, EMPTY_STRING, null,EMPTY_STRING,EMPTY_STRING));
        }
        return currentNote;
    }

    public static synchronized void setInstance(Note note) {
        currentNote = new CurrentNote(Note.clone(note));
    }

    public synchronized void clearCurrentNote() {
        note = new Note(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, new Date().getTime(), null, EMPTY_STRING, null,EMPTY_STRING,EMPTY_STRING);
    }

    public synchronized Note getCurrentNote() {
        return note;
    }

}

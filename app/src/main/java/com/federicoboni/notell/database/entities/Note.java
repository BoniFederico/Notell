package com.federicoboni.notell.database.entities;


import com.google.firebase.firestore.DocumentId;

public class Note {
    private String title;
    private String text;
    private String tag;
    private long date;
    private String color;
    private String imageUriPath;
    private String imageStoreName;
    private String imageRealPath;
    @DocumentId
    private String documentId;

    //Constructor needed by RecyclerView
    public Note() {

    }

    public Note(String title, String text, String tag, long date, String color, String imageUriPath, String documentId, String imageStoreName, String imageRealPath) {
        this.color = color;
        this.date = date;
        this.text = text;
        this.title = title;
        this.tag = tag;
        this.imageUriPath = imageUriPath;
        this.documentId = documentId;
        this.imageStoreName=imageStoreName;
        this.imageRealPath=imageRealPath;
    }

    public static Note clone(Note note) {
        return new Note(note.getTitle(), note.getText(), note.getTag(), note.getDate(), note.getColor(), note.getImageUriPath(), note.getDocumentId(),note.getImageStoreName(), note.getImageRealPath());

    }

    public String getImageUriPath() {
        return imageUriPath;
    }

    public void setImageUriPath(String imageUriPath) {
        this.imageUriPath = imageUriPath;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getImageStoreName() {
        return imageStoreName;
    }

    public void setImageStoreName(String imageStoreName) {
        this.imageStoreName = imageStoreName;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getImageRealPath() {
        return imageRealPath;
    }

    public void setImageRealPath(String imageRealPath) {
        this.imageRealPath = imageRealPath;
    }

    public enum fields {
        TITLE, TEXT, DATE, TAG, COLOR, IMAGE
    }
}

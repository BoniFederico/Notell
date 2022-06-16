package com.federicoboni.notell.database.dao;


import android.net.Uri;

import com.federicoboni.notell.database.entities.Note;
import com.federicoboni.notell.utils.ConfigUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NoteDao {
    private static final String COLLECTION_NAME = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.NOTES_COLL_NAME);
    private static final String DOCUMENT_NAME = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.NOTES_DOC_NAME);
    private static final String ORDERED_BY = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.NOTE_ORDER_PROP);
    private static final String NOTE_IMAGE_FOLDER_START_NAME = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.NOTE_IMAGE_FOLDER_START_NAME);
    private static final String NOTES_SHARED_DOC_NAME = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.NOTES_SHARED_DOC_NAME);
    private static final String IMAGES_SHARED_FOLDER = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.IMAGES_SHARED_FOLDER);
    private static final String EMPTY_STRING = "";
    private static NoteDao noteDao;
    private final FirebaseFirestore firebaseFirestore;

    private NoteDao() {
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public static synchronized NoteDao getInstance() {
        if (noteDao == null) {
            noteDao = new NoteDao();
        }
        return noteDao;
    }

    public synchronized Task<List<Note>> getAllNotes() {
        Query query = firebaseFirestore.collection(COLLECTION_NAME).document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).collection(DOCUMENT_NAME).orderBy(ORDERED_BY, Query.Direction.DESCENDING);
        return query.get().continueWith(task -> task.getResult().toObjects(Note.class));
    }

    public synchronized Task<Note> getNote(String documentId) {
        DocumentReference docRef = firebaseFirestore.collection(COLLECTION_NAME).document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).collection(DOCUMENT_NAME).document(documentId);
        return docRef.get().continueWith(task -> task.getResult().toObject(Note.class));
    }
    public synchronized Task<Void> addNote(Note note) {
        if (!note.getImageUriPath().equals(EMPTY_STRING)) {
            note.setImageStoreName(UUID.randomUUID().toString());
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference docRef = firebaseFirestore.collection(COLLECTION_NAME).document(uid).collection(DOCUMENT_NAME).document();
        return docRef.set(note).continueWithTask(task -> {
            if (!note.getImageUriPath().equals(EMPTY_STRING)) {
                return addImage(note);
            } else {
                return task;
            }
        });
    }
    private synchronized Task<Void> addImage(Note note) {
        return FirebaseStorage.getInstance().getReference().child(NOTE_IMAGE_FOLDER_START_NAME + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + note.getImageStoreName()).putFile(Uri.parse(note.getImageUriPath())).continueWith(t -> null);
    }

    public synchronized Task<Void> updateNote(Note note) {
        DocumentReference docRef = firebaseFirestore.collection(COLLECTION_NAME).document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).collection(DOCUMENT_NAME).document(note.getDocumentId());
        return getNote(note.getDocumentId()).continueWithTask(task -> {
            Note oldNote = task.getResult();
            if (!oldNote.getImageRealPath().equals(note.getImageRealPath())) {
                if (!note.getImageRealPath().equals(EMPTY_STRING)) {
                    note.setImageStoreName(UUID.randomUUID().toString());
                    return oldNote.getImageRealPath().equals(EMPTY_STRING) ? addImage(note) : deleteImage(oldNote).continueWithTask(t -> addImage(note));
                } else if (!oldNote.getImageRealPath().equals(EMPTY_STRING)) {
                    return deleteImage(oldNote);
                }
            }
            return task.continueWith(t -> null);
        }).continueWithTask(a -> docRef.set(note));
    }

    public synchronized Task<Void> updateNoteWithoutImage(Note note) {
        DocumentReference docRef = firebaseFirestore.collection(COLLECTION_NAME).document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).collection(DOCUMENT_NAME).document(note.getDocumentId());
        return docRef.set(note);
    }

    public synchronized Task<Void> addOrUpdateNote(Note note) {
        return note.getDocumentId() == null ? addNote(note) : updateNote(note);
    }

    public synchronized Task<Void> deleteNote(Note note) {
        if (note.getDocumentId() == null) {
            return null;
        } else {
            DocumentReference docRef = firebaseFirestore.collection(COLLECTION_NAME).document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).collection(DOCUMENT_NAME).document(note.getDocumentId());
            return note.getImageRealPath().equals(EMPTY_STRING) ? docRef.delete() : docRef.delete().continueWithTask(task -> deleteImage(note));
        }
    }

    private synchronized Task<Void> deleteImage(Note note) {
        if (note.getDocumentId() == null || note.getImageRealPath().equals("")) {
            return null;
        } else {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(NOTE_IMAGE_FOLDER_START_NAME + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + note.getImageStoreName());
            return storageReference.delete();
        }
    }

    public synchronized Task<Void> shareNote(Note note, String id) {
        DocumentReference docRef = firebaseFirestore.collection(COLLECTION_NAME).document(NOTES_SHARED_DOC_NAME).collection(id).document();
        if (!note.getImageRealPath().equals(EMPTY_STRING)) {
            String path = IMAGES_SHARED_FOLDER + "/" + note.getImageStoreName();
            try {
                File localFile = File.createTempFile("Images", "bmp");

                return FirebaseStorage.getInstance().getReference().child(NOTE_IMAGE_FOLDER_START_NAME + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + note.getImageStoreName()).getFile(localFile).continueWithTask(task -> FirebaseStorage.getInstance().getReference().child(path).putFile(Uri.parse(localFile.toURI().toString())).continueWith(t -> null).continueWithTask(t -> docRef.set(note)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return docRef.set(note);
    }

    public synchronized Task<List<Note>> getSharedNote(String id) {
        Query query = firebaseFirestore.collection(COLLECTION_NAME).document(NOTES_SHARED_DOC_NAME).collection(id).orderBy(ORDERED_BY, Query.Direction.DESCENDING);
        return query.get().continueWith(task -> task.getResult().toObjects(Note.class)).continueWithTask(task -> {
            List<Note> sn = task.getResult();
            if (sn.isEmpty()) {
                return task.continueWith(w -> sn);
            } else {
                Note n = sn.get(0);
                if (!n.getImageRealPath().equals(EMPTY_STRING)) {
                    String path = IMAGES_SHARED_FOLDER + "/" + n.getImageStoreName();
                    File localFile = File.createTempFile("Images", "bmp");
                    return FirebaseStorage.getInstance().getReference().child(path).getFile(localFile).continueWithTask(task1 -> {
                        sn.get(0).setImageUriPath(EMPTY_STRING);
                        return FirebaseStorage.getInstance().getReference().child(NOTE_IMAGE_FOLDER_START_NAME + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + n.getImageStoreName()).putFile(Uri.parse(localFile.toURI().toString())).continueWithTask(t -> addNote(sn.get(0)).continueWithTask(w -> deleteSharedNote(id, sn.get(0))).continueWith(w -> sn));

                    });
                }
                return addNote(sn.get(0)).continueWithTask(w -> deleteSharedNote(id, sn.get(0))).continueWith(w -> sn);
            }
        });

    }

    public synchronized Task<Void> deleteSharedNote(String sharedNoteId, Note note) {
        CollectionReference colRef = firebaseFirestore.collection(COLLECTION_NAME).document(NOTES_SHARED_DOC_NAME).collection(sharedNoteId);
        return colRef.get().continueWithTask(w -> {
            List<Note> notes = w.getResult().toObjects(Note.class);
            if (notes.isEmpty()) {
                return null;
            }
            DocumentReference docRef = firebaseFirestore.collection(COLLECTION_NAME).document(NOTES_SHARED_DOC_NAME).collection(sharedNoteId).document(notes.get(0).getDocumentId());
            return notes.get(0).getImageRealPath().equals(EMPTY_STRING) ? docRef.delete() : docRef.delete().continueWithTask(t -> deleteSharedImage(note.getImageStoreName()));
        });

    }

    private synchronized Task<Void> deleteSharedImage(String imageId) {
        String path = IMAGES_SHARED_FOLDER + "/" + imageId;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(path);
        return storageReference.delete();

    }
}

package com.federicoboni.notell.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.federicoboni.notell.R;
import com.federicoboni.notell.activities.NoteActivity;
import com.federicoboni.notell.database.dao.NoteDao;
import com.federicoboni.notell.database.entities.Note;
import com.federicoboni.notell.entities.CurrentNote;
import com.federicoboni.notell.holders.NoteHolder;
import com.federicoboni.notell.utils.ConfigUtils;
import com.federicoboni.notell.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class NotesAdapter extends RecyclerView.Adapter<NoteHolder> {
    private static final int searchTimerDelay = ConfigUtils.getInstance().getIntProperty(ConfigUtils.ConfigName.WAIT_TIME_FOR_SEARCH);
    private final List<Note> allNotes;
    private final Context context;
    Timer timer;
    private List<Note> notes;

    public NotesAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
        this.allNotes = notes;

    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_card, parent, false);
        return new NoteHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {

        Note note = this.notes.get(position);
        int currentPosition = position;
        holder.bindNoteToViews(note, context);
        holder.getCardView().setOnClickListener(view -> openNote(note, view));
        holder.getCardView().setOnLongClickListener(view -> {
            deleteNote(note, view, currentPosition);
            return false;
        });
        holder.getMenuItem().setOnClickListener(view -> {
            PopupMenu menu = new PopupMenu(view.getContext(), view);
            menu.setGravity(Gravity.END);
            menu.getMenu().add(context.getResources().getString(R.string.menu_note_edit)).setOnMenuItemClickListener(menuItem -> {
                openNote(note, view);
                return false;
            });

            menu.getMenu().add(context.getResources().getString(R.string.menu_note_share)).setOnMenuItemClickListener(menuItem -> {
                NoteDao noteDao = NoteDao.getInstance();
                String sharedNoteId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                TextView code = new TextView(view.getContext());
                code.setText(context.getResources().getString(R.string.alert_share_mex) + " " + sharedNoteId);
                code.setGravity(Gravity.CENTER);
                code.setLineSpacing(70, 1);
                code.setPadding(20, 20, 20, 20);
                code.setTextSize(16);
                code.setTextIsSelectable(true);
                builder.setView(code);
                builder
                        .setCancelable(false)
                        .setPositiveButton(context.getResources().getString(R.string.alert_yes), (dialog1, id) -> noteDao.shareNote(note, sharedNoteId)
                                .addOnCompleteListener(task -> {
                                    Toast.makeText(context, context.getResources().getString(R.string.toast_alert_share_successful), Toast.LENGTH_SHORT).show();
                                    Logger.i(Logger.SCOPE.NOTE_ADAPTER, Logger.ACTION.NOTE_SHARED);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, context.getResources().getString(R.string.toast_alert_share_failure), Toast.LENGTH_SHORT).show();
                                    Logger.e(Logger.SCOPE.NOTE_ADAPTER, e.getMessage());
                                }))
                        .setNegativeButton(context.getResources().getString(R.string.alert_no), (dialog, id) -> dialog.cancel());
                builder.create().show();
                return false;
            });
            menu.getMenu().add(context.getResources().getString(R.string.menu_note_delete)).setOnMenuItemClickListener(menuItem -> {
                deleteNote(note, view, currentPosition);
                return false;
            });
            menu.show();
        });

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public long getItemId(int position) {
        return notes.get(position).getDocumentId().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public void filterNotes(final String search) {
        if (search == null) {
            return;
        }
        resetTimer();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (search.trim().isEmpty()) {
                    notes = allNotes;
                } else {
                    List<Note> list = new ArrayList<>();
                    for (Note note : allNotes) {
                        if (note.getTitle().toLowerCase().contains(search.toLowerCase()) || note.getText().toLowerCase().contains(search.toLowerCase()) || note.getTag().toLowerCase().contains(search.toLowerCase())) {
                            list.add(note);
                        }
                    }
                    notes = list;
                }
                //Perform the notification in the main Thread, not in the one that has called the filterNotes method
                new Handler(Looper.getMainLooper()).post(() -> notifyItemRangeChanged(0, notes.size()));
            }
        }, searchTimerDelay);
    }

    public void resetTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void removeNote(int position) {
        notes.remove(position);
        notifyItemRemoved(position);
    }

    private void openNote(Note note, View fromView) {
        CurrentNote.setInstance(note);
        Intent intent = new Intent(fromView.getContext(), NoteActivity.class);
        fromView.getContext().startActivity(intent);
    }

    private void deleteNote(Note note, View view, int currentPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage(context.getResources().getString(R.string.alert_delete_note))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.alert_yes), (dialog1, id) -> {
                    NoteDao noteDao = NoteDao.getInstance();
                    noteDao.deleteNote(note)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(view.getContext(), context.getResources().getString(R.string.toast_note_delete_success), Toast.LENGTH_SHORT).show();
                                Logger.i(Logger.SCOPE.NOTE_ADAPTER, Logger.ACTION.NOTE_DELETED);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(view.getContext(), context.getResources().getString(R.string.toast_note_delete_failure), Toast.LENGTH_SHORT).show();
                                Logger.e(Logger.SCOPE.NOTE_ADAPTER, e.getMessage());
                            }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            removeNote(currentPosition);
                        }
                    });
                })
                .setNegativeButton(context.getResources().getString(R.string.alert_no), (dialog, id) -> dialog.cancel());
        builder.create().show();
    }
}
package com.federicoboni.notell.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.federicoboni.notell.R;
import com.federicoboni.notell.adapters.NotesAdapter;
import com.federicoboni.notell.database.dao.NoteDao;
import com.federicoboni.notell.database.dao.UserDao;
import com.federicoboni.notell.database.entities.Note;
import com.federicoboni.notell.entities.CurrentNote;
import com.federicoboni.notell.entities.UserLabel;
import com.federicoboni.notell.entities.WaitingDialog;
import com.federicoboni.notell.utils.ConfigUtils;
import com.federicoboni.notell.utils.Logger;
import com.federicoboni.notell.utils.ScreenUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DashboardActivity extends AppCompatActivity {
    private static final String IMAGE_IN_PREV = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.IMAGE_IN_PREV_PROPERTY_NAME);
    private static final String NUM_OF_COLS = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.NUM_OF_COLS_PROPERTY_NAME);

    FloatingActionButton createNote;
    RecyclerView noteContainer;
    StaggeredGridLayoutManager layoutManager;
    Toolbar toolbar;
    NotesAdapter recyclerAdapter;
    SearchView searchView;
    WaitingDialog waitingDialog;
    private Boolean showImagePreview;
    private int numOfColumns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Check permission for images
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DashboardActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Clear the current note
        CurrentNote.getInstance().clearCurrentNote();

        //Bind variables to views
        createNote = findViewById(R.id.fab_dash_add_note);
        noteContainer = findViewById(R.id.rcv_dash_note_list);
        searchView = findViewById(R.id.sv_dash_search_bar);
        toolbar = findViewById(R.id.tb_dash_toolbar);

        //Set ActionBar (only if we aren't in a smartphone in landscape mode):
        if (!(new ScreenUtils(getApplicationContext()).isScreenNormalOrSmall().isLandscape().build())) {
            try {
                toolbar.setVisibility(View.VISIBLE);
                setSupportActionBar(toolbar);
                Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.dashboard_title, UserDao.getInstance().getUsername()));
            } catch (Exception e) {
                Logger.e(Logger.SCOPE.DASHBOARD_ACTIVITY, e.getMessage());
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_cannot_set_action_bar), Toast.LENGTH_SHORT).show();
            }
        }

        //Set Waiting Dialog until notes are loaded:
        waitingDialog = new WaitingDialog(DashboardActivity.this, R.layout.dialog_wait);
        waitingDialog.openLoadingDialog();

        //Get preferences for display notes:
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        numOfColumns = sp.getBoolean(NUM_OF_COLS, false) ? 1 : 2;
        showImagePreview = sp.getBoolean(IMAGE_IN_PREV, true);

        //Set Layout Manager:
        layoutManager = new StaggeredGridLayoutManager(numOfColumns, StaggeredGridLayoutManager.VERTICAL) {
            @Override
            public void onLayoutCompleted(RecyclerView.State state) {
                waitingDialog.dismissDialog();
                super.onLayoutCompleted(state);
            }
        };

        noteContainer.setHasFixedSize(true);
        noteContainer.setItemAnimator(null);
        noteContainer.setLayoutManager(layoutManager);

        setRecyclerView();

        //Set Listener for SearchBar:
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s != null && recyclerAdapter != null) {
                    recyclerAdapter.filterNotes(s);
                }
                return true;
            }
        });

        //Set listener for creating new note:
        createNote.setOnClickListener(view -> startActivity(new Intent(DashboardActivity.this, NoteActivity.class)));

        //Set listener for import shared note:
        createNote.setOnLongClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            EditText e = new EditText(getApplicationContext());
            e.setWidth(50);
            e.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));
            e.setHint(getResources().getString(R.string.alert_hint_sh_code));
            e.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey_3));
            e.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_0));
            e.setGravity(Gravity.CENTER);
            builder.setView(e);
            builder.setMessage(getResources().getString(R.string.alert_add_share_mex))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.alert_yes), (dialog1, id) -> {
                        waitingDialog = new WaitingDialog(DashboardActivity.this, R.layout.dialog_wait);
                        waitingDialog.openLoadingDialog();
                        NoteDao.getInstance().getSharedNote(e.getText().toString()).addOnFailureListener(e1 -> {
                            Logger.e(Logger.SCOPE.DASHBOARD_ACTIVITY, e1.getMessage());
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_cannot_load_shared_note), Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {
                            if (task.getResult().isEmpty()) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_alert_get_share_failure), Toast.LENGTH_SHORT).show();
                                Logger.i(Logger.SCOPE.DASHBOARD_ACTIVITY, getResources().getString(R.string.toast_alert_get_share_failure));
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_alert_get_share_successful), Toast.LENGTH_SHORT).show();
                                Logger.i(Logger.SCOPE.DASHBOARD_ACTIVITY, getResources().getString(R.string.toast_alert_get_share_successful));
                                setRecyclerView();
                            }
                            waitingDialog.dismissDialog();
                        });
                    })
                    .setNegativeButton(getResources().getString(R.string.alert_no), (dialog, id) -> dialog.cancel());
            builder.create().show();
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_preview, menu);
        return true;
    }

    @Override
    protected void onResume() {
        checkSettingsChanges();
        super.onResume();
        CurrentNote.getInstance().clearCurrentNote();
        setRecyclerView();
    }

    private void checkSettingsChanges() {
        if (recyclerAdapter != null && layoutManager != null) {
            if ((PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(NUM_OF_COLS, false) ? 1 : 2) != numOfColumns) {
                numOfColumns = numOfColumns == 1 ? 2 : 1;
                layoutManager = new StaggeredGridLayoutManager(numOfColumns, StaggeredGridLayoutManager.VERTICAL);
                noteContainer.setItemAnimator(null);
                noteContainer.setLayoutManager(layoutManager);
                noteContainer.setAdapter(recyclerAdapter);
                Logger.i(Logger.SCOPE.DASHBOARD_ACTIVITY, Logger.ACTION.SETTING_CHANGED);
            }
            if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(IMAGE_IN_PREV, false) != showImagePreview) {
                noteContainer.setAdapter(recyclerAdapter);
                Logger.i(Logger.SCOPE.DASHBOARD_ACTIVITY, Logger.ACTION.SETTING_CHANGED);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
        } else if (item.getItemId() == R.id.logout) {
            getAlertDialog(getResources().getString(R.string.alert_log_out_text), (dialog, id) -> {
                UserDao.getInstance().logOut();
                finish();
                startActivity(new Intent(DashboardActivity.this, AuthenticationActivity.class));

                dialog.cancel();
                Logger.i(Logger.SCOPE.DASHBOARD_ACTIVITY, Logger.ACTION.LOG_OUT);
            }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private AlertDialog getAlertDialog(String msg, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.alert_yes), listener)
                .setNegativeButton(getResources().getString(R.string.alert_no), (dialog, id) -> dialog.cancel());
        return builder.create();
    }

    public void setRecyclerView() {
        NoteDao.getInstance().getAllNotes().addOnCompleteListener(task -> {
            List<Note> currentNotes = task.getResult();
            recyclerAdapter = new NotesAdapter(getApplicationContext(), currentNotes);
            recyclerAdapter.hasStableIds();
            noteContainer.setAdapter(recyclerAdapter);
            noteContainer.setItemViewCacheSize(ConfigUtils.getInstance().getIntProperty(ConfigUtils.ConfigName.NUM_OF_CACHED_IMG));
            noteContainer.setDrawingCacheEnabled(true);
            noteContainer.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);  // Improving scrolling performances
            UserLabel.setInstance(currentNotes.stream().map(Note::getTag).collect(Collectors.toList()));
        });
    }
}
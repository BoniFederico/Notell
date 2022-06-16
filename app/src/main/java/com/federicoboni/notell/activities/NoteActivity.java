package com.federicoboni.notell.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.federicoboni.notell.R;
import com.federicoboni.notell.database.dao.NoteDao;
import com.federicoboni.notell.database.entities.Note;
import com.federicoboni.notell.entities.CurrentNote;
import com.federicoboni.notell.entities.UserLabel;
import com.federicoboni.notell.utils.ConfigUtils;
import com.federicoboni.notell.utils.DateUtils;
import com.federicoboni.notell.utils.ImageUtils;
import com.federicoboni.notell.utils.Logger;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class NoteActivity extends AppCompatActivity {

    private static long mLastClickTime = 0;
    private Animation fromBottom;
    private Animation toBottom;
    private Animation open;
    private Animation close;
    private FloatingActionButton floatingActionButtonAddImage;
    private FloatingActionButton floatingActionButtonColor;
    private FloatingActionButton floatingActionButtonColorRed;
    private FloatingActionButton floatingActionButtonColorGreen;
    private FloatingActionButton floatingActionButtonColorYellow;
    private FloatingActionButton floatingActionButtonColorBlue;
    private FloatingActionButton buttonSave;
    private ImageView noteImage;
    private ImageView tagImage;
    private ImageView noteMenuIcon;
    private ConstraintLayout colorSelectionLayout;
    private EditText noteContent;
    private EditText noteTitle;
    private TextView dateLabel;
    private AutoCompleteTextView noteTagTextView;
    private Toolbar toolbar;
    private boolean areColorsShowed = false;
    private Note currentNote;
    private ActivityResultLauncher<Intent> imageActivityRegister;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Inflate the GUI:
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        //Bind all the view variables to layout
        bindVariablesToLayout();

        //Set Custom action bar
        try {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            Logger.e(Logger.SCOPE.NOTE_ACTIVITY, e.getMessage());
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_cannot_set_action_bar), Toast.LENGTH_SHORT).show();
        }


        //Set imageActivityRegister:
        imageActivityRegister = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> onGetImageActivityResult(result.getResultCode(), result.getData()));

        //Get the currentNote Instance:
        currentNote = CurrentNote.getInstance().getCurrentNote();

        //Check and update currentNote color if null:
        currentNote.setColor(currentNote.getColor() == null ? getResources().getString(R.color.notell_green) : currentNote.getColor());


        //bind currentNote values to corresponding layout properties:
        bindCurrentNoteToLayout();

        //Set Tags array and relative listener:
        List<String> labels = new ArrayList<>();
        try {
            labels = UserLabel.getInstance().getLabels();
        } catch (Exception e) {
            Logger.e(Logger.SCOPE.NOTE_ACTIVITY, e.getMessage());
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_cannot_set_tags), Toast.LENGTH_SHORT).show();
        }
        noteTagTextView.setAdapter(new ArrayAdapter<>(NoteActivity.this, android.R.layout.simple_dropdown_item_1line, labels));
        noteTagTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                noteTagTextView.showDropDown();
            }
        });
        //Set Listener for Add Image button:
        floatingActionButtonAddImage.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(NoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                selectImageToAdd();
            }
        });
        //Set Listener for space-to-list feature:
        noteContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                if (e.toString().startsWith(" ") || e.toString().startsWith("-")) {
                    String replacement = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.LIST_CHAR) + " ";
                    Editable ab = new SpannableStringBuilder(e.toString().replaceFirst(" ", replacement).replaceFirst("-", replacement));
                    e.replace(0, e.length(), ab);
                }
                if (e.toString().contains("\n-") || e.toString().contains(("\n "))) {
                    String replacement = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.LIST_CHAR) + " ";
                    Editable ab = new SpannableStringBuilder(e.toString().replace("\n ", "\n" + replacement).replace("\n-", "\n" + replacement));
                    e.replace(0, e.length(), ab);
                }
            }
        });
        //Listener for image deleting on long click:
        noteImage.setOnLongClickListener(view -> {
            AlertDialog dialog = getAlertDialog(getResources().getString(R.string.alert_delete_image_text), (dialog1, id) -> {
                currentNote.setImageUriPath("");
                currentNote.setImageRealPath("");
                currentNote.setImageStoreName("");
                bindCurrentNoteToLayout(Note.fields.IMAGE);
                dialog1.cancel();
            });
            dialog.show();
            return true;
        });
        //Listener for saving note:
        buttonSave.setOnClickListener(view -> {
            //Solution for multiple clicks found at: https://stackoverflow.com/questions/5608720/android-preventing-double-click-on-a-button#:~:text=The%20actual%20solution%20to%20this,seem%20to%20be%20very%20effective.
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) { // 1000 = 1second
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            buttonSave.setEnabled(false);
            currentNote.setTitle(noteTitle.getText().toString());
            currentNote.setText(noteContent.getText().toString());
            currentNote.setTag(noteTagTextView.getText().toString());
            currentNote.setDate(new Date().getTime());
            if (currentNote.getTitle().isEmpty()) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_note_cannot_save_empty), Toast.LENGTH_SHORT).show();
            } else {
                addOrUpdateNote();
            }
            buttonSave.setEnabled(true);
        });
        noteTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {  // on lost focus
                noteTitle.setSelection(0, 0);
            }
        });
        floatingActionButtonColorBlue.setOnClickListener(view -> {
            int blue = R.color.notell_blue;
            changeNoteColor(blue);
            changeColorsStatus();
        });
        floatingActionButtonColorGreen.setOnClickListener(view -> {
            int green = R.color.notell_green;
            changeNoteColor(green);
            changeColorsStatus();
        });
        floatingActionButtonColorYellow.setOnClickListener(view -> {
            int yellow = R.color.notell_yellow;
            changeNoteColor(yellow);
            changeColorsStatus();
        });
        floatingActionButtonColorRed.setOnClickListener(view -> {
            int red = R.color.notell_red;
            changeNoteColor(red);
            changeColorsStatus();
        });
        floatingActionButtonColor.setOnClickListener(view -> changeColorsStatus());
        noteMenuIcon.setOnClickListener(view -> {
            PopupMenu menu = new PopupMenu(view.getContext(), view);
            menu.setGravity(Gravity.END);
            menu.getMenu().add(getResources().getString(R.string.menu_note_delete)).setOnMenuItemClickListener(menuItem -> {
                if (currentNote.getDocumentId() != null) {
                    NoteDao.getInstance().deleteNote(currentNote).addOnSuccessListener(unused -> {
                        Toast.makeText(view.getContext(), getResources().getString(R.string.toast_note_delete_success), Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(new Intent(NoteActivity.this, DashboardActivity.class));
                    }).addOnFailureListener(e -> {
                        Toast.makeText(view.getContext(), getResources().getString(R.string.toast_note_delete_failure), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    });
                } else {
                    onBackPressed();
                }
                return false;
            });
            menu.show();
        });
    }

    private void changeNoteColor(int color) {
        CurrentNote.getInstance().getCurrentNote().setColor(getResources().getString(color));
        bindCurrentNoteToLayout(Note.fields.COLOR);
    }

    private void changeColorsStatus() {
        setVisibility(areColorsShowed);
        setAnimation(areColorsShowed);
        areColorsShowed = !areColorsShowed;
    }

    private void setVisibility(boolean areColorsShowed) {
        if (areColorsShowed) {
            colorSelectionLayout.setVisibility(View.INVISIBLE);
            floatingActionButtonAddImage.setVisibility(View.VISIBLE);
        } else {
            colorSelectionLayout.setVisibility(View.VISIBLE);
            floatingActionButtonAddImage.setVisibility(View.INVISIBLE);
        }
    }

    private void setAnimation(boolean areColorsShowed) {
        if (areColorsShowed) {
            floatingActionButtonColor.startAnimation(close);
            colorSelectionLayout.startAnimation(toBottom);
            colorSelectionLayout.postDelayed(() -> {
                colorSelectionLayout.clearAnimation();
                colorSelectionLayout.setVisibility(View.GONE);
            }, toBottom.getDuration());
            floatingActionButtonAddImage.startAnimation(fromBottom);
        } else {
            floatingActionButtonColor.startAnimation(open);
            colorSelectionLayout.startAnimation(fromBottom);
            floatingActionButtonAddImage.startAnimation(toBottom);
            floatingActionButtonAddImage.postDelayed(() -> {
                floatingActionButtonAddImage.clearAnimation();
                floatingActionButtonAddImage.setVisibility(View.GONE);
            }, toBottom.getDuration());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    private void selectImageToAdd() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (getPackageManager().resolveActivity(intent, 0) != null) {
            imageActivityRegister.launch(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageToAdd();
            } else {
                Toast.makeText(this, getResources().getString(R.string.toast_note_set_permission_for_images), Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onGetImageActivityResult(int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri selectedUri = data.getData();
                InputStream inputStream = getContentResolver().openInputStream(selectedUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                noteImage.setImageBitmap(ImageUtils.setImageCorrectRotation(bitmap, ImageUtils.getImageRealPathFromURI(getContentResolver(), selectedUri)));
                noteImage.setVisibility(View.VISIBLE);

                CurrentNote.getInstance().getCurrentNote().setImageRealPath(ImageUtils.getImageRealPathFromURI(getContentResolver(), selectedUri));
                CurrentNote.getInstance().getCurrentNote().setImageUriPath(selectedUri.toString());
            } catch (IOException e) {
                Toast.makeText(this, getResources().getString(R.string.toast_note_generic_error_image), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void bindVariablesToLayout() {
        floatingActionButtonColor = findViewById(R.id.fab_note_show_colors_icon);
        floatingActionButtonColorRed = findViewById(R.id.fab_note_red_icon);
        floatingActionButtonColorGreen = findViewById(R.id.fab_note_green_icon);
        floatingActionButtonColorYellow = findViewById(R.id.fab_note_yellow_icon);
        floatingActionButtonColorBlue = findViewById(R.id.fab_note_blue_icon);
        floatingActionButtonAddImage = findViewById(R.id.fab_note_add_img_icon);
        noteImage = findViewById(R.id.image_note_picture);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.action_button_from_bottom);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.action_button_to_bottom);
        open = AnimationUtils.loadAnimation(this, R.anim.action_button_rotate_open);
        close = AnimationUtils.loadAnimation(this, R.anim.action_button_rotate_close);
        noteContent = findViewById(R.id.edit_note_body);
        noteTitle = findViewById(R.id.edit_note_title);
        buttonSave = findViewById(R.id.fab_note_save_icon);
        dateLabel = findViewById(R.id.text_note_date);
        tagImage = findViewById(R.id.image_note_tag);
        noteTagTextView = findViewById(R.id.text_note_tag);
        colorSelectionLayout = findViewById(R.id.csl_note_color_icons);
        toolbar = findViewById(R.id.tb_note_toolbar);
        noteMenuIcon = findViewById(R.id.image_note_menu);
    }

    private void bindCurrentNoteToLayout(@NonNull Note.fields field) {
        switch (field) {
            case TITLE:
                noteTitle.setText(currentNote.getTitle());
                break;
            case TEXT:
                noteContent.setText(currentNote.getText());
                break;
            case TAG:
                noteTagTextView.setText(currentNote.getTag());
                break;
            case DATE:
                dateLabel.setText(DateUtils.returnCurrentDateTimeFormat(currentNote.getDate()));
                break;
            case COLOR:
                Drawable drawable = tagImage.getBackground().mutate();
                DrawableCompat.setTint(drawable, Color.parseColor(currentNote.getColor()));
                noteContent.setHighlightColor(Color.parseColor(currentNote.getColor()));
                noteTitle.setHighlightColor(Color.parseColor(currentNote.getColor()));
                break;
            case IMAGE:
                try {
                    Bitmap img = Objects.equals(currentNote.getImageRealPath(), "") ? null : BitmapFactory.decodeFile(currentNote.getImageRealPath());
                    noteImage.setImageDrawable(null);
                    noteImage.setVisibility(View.GONE);
                    if (img != null) {
                        noteImage.setImageBitmap(ImageUtils.setImageCorrectRotation(img, currentNote.getImageRealPath()));
                        noteImage.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Logger.e(Logger.SCOPE.NOTE_ACTIVITY, e.getMessage());
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_cannot_load_image), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void bindCurrentNoteToLayout() {
        Arrays.stream(Note.fields.values()).forEach(this::bindCurrentNoteToLayout);
    }

    private AlertDialog getAlertDialog(String msg, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.alert_yes), listener)
                .setNegativeButton(getResources().getString(R.string.alert_no), (dialog, id) -> dialog.cancel());
        return builder.create();
    }

    private void addOrUpdateNote() {
        NoteDao.getInstance().addOrUpdateNote(currentNote).addOnSuccessListener(unused -> {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_note_created_successful), Toast.LENGTH_SHORT).show();
            finish();
            Logger.i(Logger.SCOPE.NOTE_ACTIVITY, Logger.ACTION.NOTE_ADDED);
            startActivity(new Intent(NoteActivity.this, DashboardActivity.class));
        }).addOnFailureListener(e -> {
            Logger.e(Logger.SCOPE.NOTE_ACTIVITY, e.getMessage());
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_note_created_failure), Toast.LENGTH_SHORT).show();
        });

    }

}
package com.federicoboni.notell.holders;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.federicoboni.notell.R;
import com.federicoboni.notell.database.dao.NoteDao;
import com.federicoboni.notell.database.entities.Note;
import com.federicoboni.notell.utils.ConfigUtils;
import com.federicoboni.notell.utils.DateUtils;
import com.federicoboni.notell.utils.ImageUtils;
import com.federicoboni.notell.utils.Logger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.ExecutionException;

public class NoteHolder extends RecyclerView.ViewHolder {

    private static final String IMAGE_IN_PREV = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.IMAGE_IN_PREV_PROPERTY_NAME);
    private static final int MAX_CHAR_PREVIEW = ConfigUtils.getInstance().getIntProperty(ConfigUtils.ConfigName.MAX_LENGTH_TEXT_PREV);
    private static final int MAX_ROWS_PREVIEW = ConfigUtils.getInstance().getIntProperty(ConfigUtils.ConfigName.MAX_ROWS_TEXT_PREV);
    private static final String END_CHARS_PREVIEW = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.END_CHAR_TEXT_PREV);
    private static final String NOTE_IMAGE_FOLDER_START_NAME = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.NOTE_IMAGE_FOLDER_START_NAME);
    private final TextView title;
    private final TextView content;
    private final ImageView imageTag;
    private final ImageView notePreviewColorBanner;
    private final TextView datePreviewTag;
    private final TextView tag;
    private final ImageView image;
    private final ImageView menuItem;
    private final CardView cardView;

    public NoteHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.csl_ntcard_title);
        content = itemView.findViewById(R.id.text_ntcard_body_preview);
        imageTag = itemView.findViewById(R.id.image_ntcard_tag);
        notePreviewColorBanner = itemView.findViewById(R.id.image_ntcard_banner);
        tag = itemView.findViewById(R.id.text_ntcard_tag);
        image = itemView.findViewById(R.id.image_ntcard_pic_preview);
        datePreviewTag = itemView.findViewById(R.id.text_ntcard_date);
        menuItem = itemView.findViewById(R.id.image_ntcard_menu);
        cardView = itemView.findViewById(R.id.card_ntcard_container);

    }

    public ImageView getMenuItem() {
        return menuItem;
    }

    public CardView getCardView() {
        return cardView;
    }

    @SuppressLint("ResourceType")
    public void bindNoteToViews(Note note, Context context) {

        this.title.setText(note.getTitle());
        this.content.setText(setNoteTextPreview(note.getText()));
        DrawableCompat.setTintList(imageTag.getBackground(), null); //Fixing bug in some devices
        DrawableCompat.setTint(imageTag.getBackground(), Color.parseColor(note.getColor()));
        DrawableCompat.setTint(notePreviewColorBanner.getBackground(), Color.parseColor(note.getColor()));
        this.tag.setText(""); //Fixing bug in some devices
        this.tag.setText(note.getTag());
        this.datePreviewTag.setText(DateUtils.returnCurrentDateTimeFormat(note.getDate()));

        this.image.setImageDrawable(null);
        this.image.setVisibility(View.INVISIBLE);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(IMAGE_IN_PREV, false) && note.getImageRealPath() != "" && note.getImageRealPath() != null) {
            Bitmap img = null;
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = getSampleSizeFromImage(note.getImageRealPath());
                img = BitmapFactory.decodeFile(note.getImageRealPath(), opts);
            } catch (Exception e) {
                Logger.e(Logger.SCOPE.NOTE_HOLDER, e.getMessage());
            }

            //If image exist in the device, then load it:
            if (img != null) {
                this.image.setImageBitmap(ImageUtils.setImageCorrectRotation(img, note.getImageRealPath()));
                this.image.setVisibility(View.VISIBLE);
            } else {
                //Get image from cloud:
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(NOTE_IMAGE_FOLDER_START_NAME + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + note.getImageStoreName());
                Glide.with(this.image.getContext())
                        .load(storageReference)
                        .into(this.image);
                this.image.setVisibility(View.VISIBLE);
                ImageView imgV = this.image;
                //Start new task for saving cloud image into user device:
                AsyncTask.execute(() -> {
                    try {
                        Bitmap bm = Glide.with(imgV.getContext()).asBitmap().load(storageReference).submit().get();
                        String path = MediaStore.Images.Media.insertImage(imgV.getContext().getContentResolver(), bm, note.getImageStoreName(), note.getTitle());
                        note.setImageUriPath(path);
                        note.setImageRealPath(ImageUtils.getImageRealPathFromURI(imgV.getContext().getContentResolver(), Uri.parse(note.getImageUriPath())));
                        NoteDao.getInstance().updateNoteWithoutImage(note);
                    } catch (ExecutionException | InterruptedException e) {
                        Toast.makeText(context, context.getResources().getString(R.string.toast_problem_store_img), Toast.LENGTH_SHORT).show();
                        Logger.e(Logger.SCOPE.NOTE_HOLDER, e.getMessage());
                    }
                });
            }
        }
    }

    private String setNoteTextPreview(String s) {
        int maxLength = MAX_CHAR_PREVIEW;
        int maxRows = MAX_ROWS_PREVIEW;
        String text = s.replaceAll("\r\n|\r", "\n");
        for (int i = 0; i < text.length(); i++) {
            maxLength--;
            if (text.charAt(i) == '\n') {
                maxRows--;
            }
            if (maxLength <= 0 || maxRows <= 1) {
                return text.substring(0, i - 1) + END_CHARS_PREVIEW;
            }
        }
        return text;
    }

    private int getSampleSizeFromImage(String path) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        //It's also possible to find quite easily the nearest 2 power for sampling (Integer.SIZE-Integer.numberOfLeadingZeros(num-1)),
        // but it doesn't seem to be very effective with respect to the following one.
        return Math.min(opts.outWidth, opts.outHeight) / 300;
    }
}

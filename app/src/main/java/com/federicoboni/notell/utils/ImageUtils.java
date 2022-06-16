package com.federicoboni.notell.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;

public class ImageUtils {
    public static Bitmap setImageCorrectRotation(Bitmap bitmap, String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
        } catch (IOException e) {
            return null;
        }
    }

    public static String getImageRealPathFromURI(ContentResolver contentResolver, Uri uri) {
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor==null){
            return null;
        }
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String path = cursor.getString(idx);
        cursor.close();
        return path;
    }
}

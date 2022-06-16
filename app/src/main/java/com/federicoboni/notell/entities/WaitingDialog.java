package com.federicoboni.notell.entities;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.federicoboni.notell.R;

public class WaitingDialog {
    private final Activity activity;
    private final int layout;
    private AlertDialog dialog;

    public WaitingDialog(Activity activity, int layout) {
        this.activity = activity;
        this.layout = layout;
    }

    public void openLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(layout, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        dialog.show();

    }

    public void dismissDialog() {
        dialog.dismiss();
    }
}

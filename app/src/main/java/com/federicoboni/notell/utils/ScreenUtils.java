package com.federicoboni.notell.utils;

import android.content.Context;
import android.content.res.Configuration;

public class ScreenUtils {
    private boolean result;
    private final Context context;

    public ScreenUtils(Context context) {
        this.result = true;
        this.context = context;
    }

    public ScreenUtils isPortrait() {
        result = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && result;
        return this;
    }

    public ScreenUtils isLandscape() {
        result = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && result;
        return this;
    }

    public ScreenUtils isScreenLarge() {
        int screenLayoutSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        result = (screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_LARGE) && result;
        return this;
    }

    public ScreenUtils isScreenNormalOrSmall() {
        int screenLayoutSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        result = (screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_SMALL || screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_NORMAL) && result;
        return this;
    }

    public boolean build() {
        return result;
    }
}

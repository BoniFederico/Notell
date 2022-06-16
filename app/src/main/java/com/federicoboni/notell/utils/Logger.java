package com.federicoboni.notell.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {
    private static final boolean DEBUG = Boolean.parseBoolean(ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.DEBUG));
    private static final boolean TIME = true;
    private static final String TIME_PATTERN="\"dd-MM-yyyy HH:mm:ss\"";
    private static final SimpleDateFormat formatter = new SimpleDateFormat(TIME_PATTERN, Locale.ITALIAN);

    public static void i(SCOPE scope, String info) {
        if (DEBUG) {
            Log.i(scope.name(), TIME ? formatter.format(new Date()) + " - " + info : info);
        }
    }

    public static void e(SCOPE scope, String info) {
        if (DEBUG) {
            Log.e(scope.name(), TIME ? formatter.format(new Date()) + " - " + info : info);
        }
    }
    public static void i(SCOPE scope, ACTION action) {
        if (DEBUG) {
            Log.i(scope.name(), TIME ? formatter.format(new Date()) + " - " + action.name() : action.name());
        }
    }

    public static void e(SCOPE scope, ACTION action) {
        if (DEBUG) {
            Log.e(scope.name(), TIME ? formatter.format(new Date()) + " - " + action.name() : action.name());
        }
    }
    public enum SCOPE {
        DASHBOARD_ACTIVITY,AUTH_ACTIVITY, NOTE_ACTIVITY,NOTE_ADAPTER, RESET_PSSW_FRAGMENT,SIGN_IN_FRAGMENT,SIGN_UP_FRAGMENT,NOTE_HOLDER
    }
    public enum ACTION {
        LOG_IN,LOG_OUT,FRAGMENT_CHANGED, SETTING_CHANGED,NOTE_ADDED, NOTE_DELETED, NOTE_SHARED,PASSWORD_CHANGED, SIGNED_IN
    }
}

package com.federicoboni.notell.utils;

import android.content.Context;

import com.federicoboni.notell.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtils {
    private static ConfigUtils configUtils;
    private final Properties properties;

    private ConfigUtils(Context context) throws IOException {
        properties = new Properties();
        InputStream inputStream = context.getResources().openRawResource(R.raw.config);
        properties.loadFromXML(inputStream);
    }

    public static void init(Context context) {
        try {
            configUtils = new ConfigUtils(context);
        } catch (IOException e) {
            configUtils = null;
        }

    }

    public static ConfigUtils getInstance() {
        return configUtils;
    }

    public String getStringProperty(ConfigName configName) {
        return properties.getProperty(configName.name());
    }

    public int getIntProperty(ConfigName configName) {
        return Integer.parseInt(properties.getProperty(configName.name()));
    }

    public enum ConfigName {
        EMAIL_REGEX, PASSWORD_REGEX, USERNAME_REGEX, NUM_OF_COLS_PROPERTY_NAME, NOTE_ORDER_PROP,
        NUM_OF_CACHED_IMG, IMAGE_IN_PREV_PROPERTY_NAME, LIST_CHAR, WAIT_TIME_FOR_SEARCH, NOTES_COLL_NAME,
        NOTES_DOC_NAME, MAX_LENGTH_TEXT_PREV, MAX_ROWS_TEXT_PREV, END_CHAR_TEXT_PREV, DATE_PATTERN, NOTE_IMAGE_FOLDER_START_NAME,
        NOTES_SHARED_DOC_NAME, IMAGES_SHARED_FOLDER, DEBUG
    }
}

package com.federicoboni.notell.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static final String DATE_PATTERN = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.DATE_PATTERN);


    public static String returnCurrentDateTimeFormat(Date date) {
        SimpleDateFormat DateFor = new SimpleDateFormat(DATE_PATTERN);
        return DateFor.format(date);
    }

    public static String returnCurrentDateTimeFormat(long date) {
        return returnCurrentDateTimeFormat(new Date(date));
    }


}

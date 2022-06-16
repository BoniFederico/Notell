package com.federicoboni.notell.utils;


import java.util.regex.Pattern;

public class ValidationUtils {
    private static final String EMAIL_REGEX = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.EMAIL_REGEX);
    private static final String USERNAME_REGEX = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.USERNAME_REGEX);
    private static final String PASSWORD_REGEX = ConfigUtils.getInstance().getStringProperty(ConfigUtils.ConfigName.PASSWORD_REGEX);

    public static boolean isEmailValid(String email) {
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    public static boolean isUsernameValid(String username) {
        return Pattern.compile(USERNAME_REGEX).matcher(username).matches();
    }

    public static boolean isPasswordValid(String password) {
        return Pattern.compile(PASSWORD_REGEX).matcher(password).matches();
    }
}

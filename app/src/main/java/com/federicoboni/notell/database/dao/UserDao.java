package com.federicoboni.notell.database.dao;


import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;

public class UserDao {

    private static UserDao userDao;
    private String username;

    private UserDao() {

    }

    public static synchronized UserDao getInstance() {
        if (userDao == null) {
            userDao = new UserDao();
        }
        return userDao;
    }

    public synchronized boolean isLogged() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public synchronized void logOut() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().signOut();
        }
    }

    public synchronized String getUsername() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        } else if (username == null) {
            username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        }
        return username;
    }

    public synchronized Task<OpStatus> sendResetEmail(String email) {

        return FirebaseAuth.getInstance().sendPasswordResetEmail(email).continueWith(task -> {
            if (task.isSuccessful()) {
                return OpStatus.SUCCESS;
            } else {
                return OpStatus.FAILURE;
            }
        });
    }

    public synchronized Task<OpStatus> signIn(String email, String password) {
        return FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).continueWith(task -> {
            if (task.isSuccessful()) {
                return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).isEmailVerified() ? OpStatus.SUCCESS : OpStatus.USER_NOT_VERIFIED;
            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                return OpStatus.CREDENTIALS_NOT_CORRECT;

            } else {
                return OpStatus.USER_NOT_EXISTS;
            }

        });
    }

    public synchronized Task<OpStatus> signUp(String email, String password, String username) {
        return signUp(email, password).onSuccessTask(authResult -> setUsername(username).onSuccessTask(unused -> sendConfirmationEmail())).continueWith(task -> {
            if (task.isSuccessful()) {
                return OpStatus.SUCCESS;
            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                return OpStatus.USER_ALREADY_PRESENT;
            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                return OpStatus.CREDENTIALS_NOT_CORRECT;
            } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                return OpStatus.CREDENTIALS_NOT_CORRECT;
            } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                return OpStatus.CANNOT_SET_USERNAME;
            } else {
                return OpStatus.GENERIC_ERROR;
            }
        });
    }

    private synchronized Task<AuthResult> signUp(String email, String password) {
        return FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password);
    }

    private synchronized Task<Void> setUsername(String username) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).updateProfile(profileUpdates);

    }

    private synchronized Task<Void> sendConfirmationEmail() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).sendEmailVerification();
    }

    public enum OpStatus {
        SUCCESS, FAILURE, USER_NOT_VERIFIED, CREDENTIALS_NOT_CORRECT, USER_NOT_EXISTS, USER_ALREADY_PRESENT, GENERIC_ERROR, CANNOT_SET_USERNAME
    }

}

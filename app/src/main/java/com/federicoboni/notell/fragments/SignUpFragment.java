package com.federicoboni.notell.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.federicoboni.notell.R;
import com.federicoboni.notell.activities.AuthenticationActivity;
import com.federicoboni.notell.database.dao.UserDao;
import com.federicoboni.notell.entities.WaitingDialog;
import com.federicoboni.notell.utils.Logger;
import com.federicoboni.notell.utils.ValidationUtils;


public class SignUpFragment extends Fragment {
    private TextView signInLabel;
    private Button buttonSignUp;
    private EditText inputPassword;
    private EditText inputUsername;
    private EditText inputEmail;
    private WaitingDialog waitingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        signInLabel = view.findViewById(R.id.text_signup_hint_acc_link);
        buttonSignUp = view.findViewById(R.id.button_signup_send);
        inputPassword = view.findViewById(R.id.edit_signup_password);
        inputUsername = view.findViewById(R.id.edit_signup_username);
        inputEmail = view.findViewById(R.id.edit_signup_email);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        waitingDialog = new WaitingDialog(getActivity(), R.layout.dialog_wait);

        buttonSignUp.setOnClickListener(v -> {
            if (!ValidationUtils.isEmailValid(inputEmail.getText().toString())) {
                Toast.makeText(requireActivity().getApplicationContext(), getResources().getString(R.string.toast_validation_email_failure), Toast.LENGTH_SHORT).show();
                return;
            } else if (!ValidationUtils.isPasswordValid(inputPassword.getText().toString())) {
                Toast.makeText(requireActivity().getApplicationContext(), getResources().getString(R.string.toast_validation_password_failure), Toast.LENGTH_LONG).show();
                return;
            } else if (!ValidationUtils.isUsernameValid(inputUsername.getText().toString())) {
                Toast.makeText(requireActivity().getApplicationContext(), getResources().getString(R.string.toast_validation_username_failure), Toast.LENGTH_LONG).show();
                return;
            }
            waitingDialog.openLoadingDialog();
            UserDao.getInstance().signUp(inputEmail.getText().toString(), inputPassword.getText().toString(), inputUsername.getText().toString()).addOnFailureListener(e -> {
                Toast.makeText(requireActivity().getApplicationContext(), getResources().getString(R.string.toast_problem_server), Toast.LENGTH_SHORT).show();
                Logger.e(Logger.SCOPE.SIGN_UP_FRAGMENT, e.getMessage());
            }).addOnCompleteListener(task -> {
                waitingDialog.dismissDialog();
                switch (task.getResult()) {
                    case SUCCESS:
                        UserDao.getInstance().logOut();
                        Toast.makeText(requireActivity(), getResources().getString(R.string.toast_sign_up_success), Toast.LENGTH_SHORT).show();
                        Logger.i(Logger.SCOPE.SIGN_UP_FRAGMENT, Logger.ACTION.SIGNED_IN);
                        break;
                    case USER_ALREADY_PRESENT:
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_sign_up_failure_already_present), Toast.LENGTH_SHORT).show();
                        Logger.i(Logger.SCOPE.SIGN_UP_FRAGMENT, UserDao.OpStatus.USER_ALREADY_PRESENT.toString());
                        break;
                    case CREDENTIALS_NOT_CORRECT:
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_sign_up_failure_not_correct), Toast.LENGTH_SHORT).show();
                        Logger.i(Logger.SCOPE.SIGN_UP_FRAGMENT, UserDao.OpStatus.CREDENTIALS_NOT_CORRECT.toString());
                        break;
                    case CANNOT_SET_USERNAME:
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_sign_up_failure_username_problem), Toast.LENGTH_SHORT).show();
                        Logger.i(Logger.SCOPE.SIGN_UP_FRAGMENT, UserDao.OpStatus.CANNOT_SET_USERNAME.toString());
                        break;
                    case GENERIC_ERROR:
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_sign_up_failure_generic_error), Toast.LENGTH_SHORT).show();
                        Logger.i(Logger.SCOPE.SIGN_UP_FRAGMENT, UserDao.OpStatus.GENERIC_ERROR.toString());
                        break;
                }
            });
        });
        signInLabel.setOnClickListener(v -> ((AuthenticationActivity) getActivity()).replaceFragment(new SignInFragment(), false));
    }
}
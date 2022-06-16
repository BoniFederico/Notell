package com.federicoboni.notell.fragments;

import android.content.Intent;
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
import com.federicoboni.notell.activities.DashboardActivity;
import com.federicoboni.notell.database.dao.UserDao;
import com.federicoboni.notell.entities.WaitingDialog;
import com.federicoboni.notell.utils.Logger;
import com.federicoboni.notell.utils.ValidationUtils;

public class SignInFragment extends Fragment {

    private TextView forgotPasswordLabel;
    private TextView signUpLabel;
    private EditText inputEmail;
    private EditText inputPassword;
    private Button buttonLogIn;
    private WaitingDialog waitingDialog;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.onCreate(savedInstanceState);

        waitingDialog = new WaitingDialog(getActivity(), R.layout.dialog_wait);

        if (UserDao.getInstance().isLogged()) {
            requireActivity().finish();
            startActivity(new Intent(getActivity(), DashboardActivity.class));
        }

        forgotPasswordLabel.setOnClickListener(v -> ((AuthenticationActivity) requireActivity()).replaceFragment(new ResetPasswordFragment(), true));

        buttonLogIn.setOnClickListener(v -> {

            if (!ValidationUtils.isEmailValid(inputEmail.getText().toString())) {
                Toast.makeText(requireActivity().getApplicationContext(), getResources().getString(R.string.toast_validation_email_failure), Toast.LENGTH_SHORT).show();
                return;
            } else if (!ValidationUtils.isPasswordValid(inputPassword.getText().toString())) {
                Toast.makeText(requireActivity().getApplicationContext(), getResources().getString(R.string.toast_validation_password_failure), Toast.LENGTH_LONG).show();
                return;
            }
            waitingDialog.openLoadingDialog();
            UserDao.getInstance().signIn(inputEmail.getText().toString(), inputPassword.getText().toString()).addOnFailureListener(e -> {
                Toast.makeText(requireActivity().getApplicationContext(), getResources().getString(R.string.toast_problem_server), Toast.LENGTH_SHORT).show();
                Logger.e(Logger.SCOPE.SIGN_IN_FRAGMENT, e.getMessage());
            }).addOnCompleteListener(task -> {
                waitingDialog.dismissDialog();
                switch (task.getResult()) {
                    case SUCCESS:
                        Logger.i(Logger.SCOPE.SIGN_IN_FRAGMENT, Logger.ACTION.LOG_IN);
                        requireActivity().finish();
                        Intent intent = new Intent(getActivity(), DashboardActivity.class);
                        startActivity(intent);
                        break;
                    case USER_NOT_VERIFIED:
                        Toast.makeText(requireActivity().getApplicationContext(), getResources().getString(R.string.toast_sign_in_failure_not_verified), Toast.LENGTH_SHORT).show();
                        Logger.i(Logger.SCOPE.SIGN_IN_FRAGMENT, UserDao.OpStatus.USER_NOT_VERIFIED.toString());
                        UserDao.getInstance().logOut();
                        break;
                    case CREDENTIALS_NOT_CORRECT:
                        Toast.makeText(requireActivity().getApplicationContext(), getResources().getString(R.string.toast_sign_in_failure_wrong), Toast.LENGTH_SHORT).show();
                        Logger.i(Logger.SCOPE.SIGN_IN_FRAGMENT, UserDao.OpStatus.CREDENTIALS_NOT_CORRECT.toString());
                        break;
                    case USER_NOT_EXISTS:
                        Toast.makeText(requireActivity().getApplicationContext(), getResources().getString(R.string.toast_sign_in_failure_not_ex), Toast.LENGTH_SHORT).show();
                        Logger.i(Logger.SCOPE.SIGN_IN_FRAGMENT, UserDao.OpStatus.USER_NOT_EXISTS.toString());
                        break;
                }
            });
        });

        signUpLabel.setOnClickListener(v -> ((AuthenticationActivity) requireActivity()).replaceFragment(new SignUpFragment(), true));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        forgotPasswordLabel = view.findViewById(R.id.text_signin_forgot_pass_link);
        signUpLabel = view.findViewById(R.id.text_signin_hint_reg_link);
        inputPassword = view.findViewById(R.id.edit_signin_password);
        inputEmail = view.findViewById(R.id.edit_signin_email);
        buttonLogIn = view.findViewById(R.id.button_signin_send);
        return view;
    }
}
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

public class ResetPasswordFragment extends Fragment {

    private TextView goBackLabel;
    private Button sendInstructionButton;
    private EditText inputEmail;
    private WaitingDialog waitingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);
        goBackLabel = view.findViewById(R.id.text_passres_go_back_link);
        sendInstructionButton = view.findViewById(R.id.button_passres_send);
        inputEmail = view.findViewById(R.id.edit_passres_email);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        waitingDialog = new WaitingDialog(getActivity(), R.layout.dialog_wait);
        goBackLabel.setOnClickListener(v -> ((AuthenticationActivity) requireActivity()).replaceFragment(new SignInFragment(), false));
        sendInstructionButton.setOnClickListener(v -> {
            if (!ValidationUtils.isEmailValid(inputEmail.getText().toString())) {
                Toast.makeText(requireActivity().getApplicationContext(), getResources().getString(R.string.toast_validation_email_failure), Toast.LENGTH_LONG).show();
                return;
            }
            waitingDialog.openLoadingDialog();
            String mail = inputEmail.getText().toString().trim();

            UserDao.getInstance().sendResetEmail(mail).addOnFailureListener(e -> {
                Toast.makeText(requireActivity().getApplicationContext(), getResources().getString(R.string.toast_problem_server), Toast.LENGTH_SHORT).show();
                Logger.e(Logger.SCOPE.RESET_PSSW_FRAGMENT, e.getMessage());
            }).addOnCompleteListener(task -> {
                waitingDialog.dismissDialog();
                switch (task.getResult()) {
                    case SUCCESS:
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_reset_pss_success), Toast.LENGTH_SHORT).show();
                        ((AuthenticationActivity) requireActivity()).replaceFragment(new SignInFragment(), false);
                        Logger.i(Logger.SCOPE.RESET_PSSW_FRAGMENT, Logger.ACTION.PASSWORD_CHANGED);
                        break;
                    case FAILURE:
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_reset_pss_failure), Toast.LENGTH_SHORT).show();
                        Logger.e(Logger.SCOPE.RESET_PSSW_FRAGMENT, getResources().getString(R.string.toast_reset_pss_failure));
                }
            });
        });
    }
}
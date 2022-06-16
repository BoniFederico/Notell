package com.federicoboni.notell.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.federicoboni.notell.R;
import com.federicoboni.notell.database.dao.UserDao;
import com.federicoboni.notell.fragments.SignInFragment;
import com.federicoboni.notell.utils.ConfigUtils;
import com.federicoboni.notell.utils.Logger;
import com.federicoboni.notell.utils.ScreenUtils;

public class AuthenticationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Init ConfigUtils class once at the start of application
        ConfigUtils.init(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        //Check if user is already logged
        if (UserDao.getInstance().isLogged()) {
            Logger.i(Logger.SCOPE.AUTH_ACTIVITY, Logger.ACTION.LOG_IN);
            finish();
            startActivity(new Intent(AuthenticationActivity.this, DashboardActivity.class));
        }
        //Setting default fragment:
        if (savedInstanceState == null) {
            replaceFragment(new SignInFragment(), true);
        }

        //Allow only tablets to navigate in this activity in landscape mode:
        if (new ScreenUtils(getApplicationContext()).isScreenNormalOrSmall().build()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public void replaceFragment(Fragment fragment, Boolean slideToRight) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //Set animations and histories while navigating through fragments
        ft.setCustomAnimations(slideToRight ? R.anim.fragment_slide_in_right : R.anim.fragment_slide_in_left, slideToRight ? R.anim.fragment_slide_out_left : R.anim.fragment_slide_out_right, R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_right);
        ft.setReorderingAllowed(true);
        if (!(fragment.getClass() == SignInFragment.class)) {
            ft.addToBackStack(SignInFragment.class.getName());
        }
        ft.replace(R.id.fl_auth_fragment_holder, fragment).commit();
        Logger.i(Logger.SCOPE.AUTH_ACTIVITY, Logger.ACTION.FRAGMENT_CHANGED);
    }
}
package com.pplugin.messo_se;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.pplugin.messo_se.ui.LoginActivity;
import com.pplugin.messo_se.ui.MainActivity;

public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is logged in
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("loggedIn", false);

        Intent intent;
        if (isLoggedIn) {
            // Launch the main activity
            intent = new Intent(this, MainActivity.class);
        } else {
            // Launch the login activity
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);

        // Finish the launcher activity to prevent going back to it
        finish();
    }

}

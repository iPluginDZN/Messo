package com.pplugin.messo_se.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SharedMemory;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pplugin.messo_se.R;

public class LoginActivity extends AppCompatActivity {
    EditText username, password;
    CheckBox rememberMe;
    Button loginButton;

    private void innit() {
        username = findViewById(R.id.username_input);
        password = findViewById(R.id.password_input);
        rememberMe = findViewById(R.id.rememberMe);
        loginButton = findViewById(R.id.login_btn);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        innit();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.getText().toString().isEmpty()) {
                    username.setError("Username is required");
                    return;
                }
                if (password.getText().toString().isEmpty()) {
                    password.setError("Password is required");
                    return;
                }
                if (username.getText().toString().equals("admin")
                        && password.getText().toString().equals("123")) {
                    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (rememberMe.isChecked()) {
                        // Save username and password
                        editor.putString("username", username.getText().toString());
                        editor.putBoolean("loggedIn", true);
                        editor.commit();
                    } else {
                        editor.putString("username", username.getText().toString());
                    }
                    // Go to main activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // Show error message
                Log.d("LoginDebug", "Username: " + username.getText().toString() + ", Password: " + password.getText().toString());
                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
            });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
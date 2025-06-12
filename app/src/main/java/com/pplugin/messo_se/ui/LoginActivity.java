package com.pplugin.messo_se.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pplugin.messo_se.R;
import com.pplugin.messo_se.services.ChatService;
import com.pplugin.messo_se.utils.InputValidation;
import com.pplugin.messo_se.utils.LoginSessionHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    CheckBox rememberMe;
    Button loginButton;
    Button registerButton;

    private void innit() {
        username = findViewById(R.id.username_input);
        password = findViewById(R.id.password_input);
        rememberMe = findViewById(R.id.rememberMe);
        loginButton = findViewById(R.id.login_btn);
        registerButton = findViewById(R.id.register_btn);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        innit();
        // Autofill username if passed from RegisterActivity
        String passedUsername = getIntent().getStringExtra("username");
        if (passedUsername != null && !passedUsername.isEmpty()) {
            username.setText(passedUsername);
        }
        InputValidation inputValidation = InputValidation.getInstance();
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
                String userInput = username.getText().toString().trim();
                String passInput = password.getText().toString();

                if (!inputValidation.isValidUsername(userInput)) {
                    username.setError("Invalid username or email.");
                    return;
                }

                if (!inputValidation.isValidPassword(passInput)) {
                    password.setError("Invalid password. Must be at least 8 characters long and contain at least one letter and one number.");
                    return;
                }

                // Disable buttons while waiting for API response
                loginButton.setEnabled(false);

                String url = "https://pplugin.works/auth/login";
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("emailOrUsername", username.getText().toString());
                    jsonBody.put("password", password.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST, url, jsonBody,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Re-enable buttons after response
                                loginButton.setEnabled(true);
                                // Handle successful login
                                try {
                                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                    String token = response.getString("token");
                                    JSONObject userObj = response.getJSONObject("user");
                                    int userId = userObj.getInt("id");
                                    String usernameStr = userObj.getString("username");
                                    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    if (rememberMe.isChecked()) {
                                        // Save username and password
                                        editor.putString("token", token);
                                        editor.putInt("userId", userId);
                                        editor.putString("username", usernameStr);
                                        editor.putBoolean("loggedIn", true);
                                        editor.apply();
                                    } else {
                                        editor.putString("token", token);
                                        editor.putInt("userId", userId);
                                        editor.putString("username", usernameStr);
                                        editor.apply();
                                    }
                                    LoginSessionHelper loginHelper = LoginSessionHelper.getInstance();
                                    loginHelper.handleLogin(LoginActivity.this, String.valueOf(userId), token);
                                    // Removed ChatService start from here, now handled in MainActivity
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("LoginError", "Error parsing response: " + e.getMessage());
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Re-enable buttons after error
                                loginButton.setEnabled(true);
                                registerButton.setEnabled(true);
                                // Handle error
                                Toast.makeText(LoginActivity.this, "Login failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("LoginError", "Error parsing response: " + error.getMessage());
                            }
                        }
                );

                queue.add(jsonObjectRequest);
//                if (username.getText().toString().equals("admin")
//                        && password.getText().toString().equals("123")) {
//                    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    if (rememberMe.isChecked()) {
//                        // Save username and password
//                        editor.putString("username", username.getText().toString());
//                        editor.putBoolean("loggedIn", true);
//                        editor.commit();
//                    } else {
//                        editor.putString("username", username.getText().toString());
//                    }
//                    // Go to main activity
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    startActivity(intent);
//                } else {
//                    // Show error message
//                Log.d("LoginDebug", "Username: " + username.getText().toString() + ", Password: " + password.getText().toString());
//                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
//                }
            }
            });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
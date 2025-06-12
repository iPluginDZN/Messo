package com.pplugin.messo_se.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.pplugin.messo_se.utils.InputValidation;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    EditText username;
    EditText email;
    EditText password;
    EditText phone;
    Button registerButton;
    Button backButton;

    private void init() {
        username = findViewById(R.id.username_input);
        email = findViewById(R.id.email_input);
        password = findViewById(R.id.password_input);
        phone = findViewById(R.id.phone_input);
        registerButton = findViewById(R.id.register_btn);
        backButton = findViewById(R.id.back_btn);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EdgeToEdge.enable(this);
        init();
        InputValidation inputValidation = InputValidation.getInstance();
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle registration logic here
                // For now, just show a toast message
                String user = username.getText().toString();
                String mail = email.getText().toString();
                String pass = password.getText().toString();
                String phoneNumber = phone.getText().toString();
                if (user.isEmpty()) {
                    username.setError("Username is required");
                    return;
                }
                if (mail.isEmpty()) {
                    email.setError("Email is required");
                    return;
                }
                if (pass.isEmpty()) {
                    password.setError("Password is required");
                    return;
                }
                if (phoneNumber.isEmpty()) {
                    phone.setError("Phone number is required");
                    return;
                }
                if(!inputValidation.isValidUsername(user)){
                    username.setError("Invalid username");
                    return;
                }
                if(!inputValidation.isValidEmail(mail)){
                    email.setError("Invalid email");
                    return;
                }
                if(!inputValidation.isValidPassword(pass)){
                    password.setError("Invalid password");
                    return;
                }
                String url = "https://pplugin.works/auth/register";
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("username", user);
                    jsonBody.put("email", mail);
                    jsonBody.put("password", pass);
                    jsonBody.put("phone", phoneNumber);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST, url, jsonBody,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                // Pass username to LoginActivity
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.putExtra("username", username.getText().toString());
                                startActivity(intent);
                                finish();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(RegisterActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                queue.add(jsonObjectRequest);
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
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

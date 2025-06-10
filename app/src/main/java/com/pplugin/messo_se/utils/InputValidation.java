package com.pplugin.messo_se.utils;

public class InputValidation {
    private static InputValidation instance;

    private InputValidation() {}

    public static InputValidation getInstance() {
        if (instance == null) {
            instance = new InputValidation();
        }
        return instance;
    }

    public boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    public boolean isValidPassword(String password) {
        // At least 8 chars, contains letter and number
        return password != null && password.length() >= 8 && password.matches(".*[A-Za-z].*") && password.matches(".*\\d.*");
    }
}

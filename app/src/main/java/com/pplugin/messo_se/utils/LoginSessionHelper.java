package com.pplugin.messo_se.utils;

import android.content.Context;
import android.util.Log;
import com.pplugin.messo_se.sqllite.SQLHelper;

public class LoginSessionHelper {
    private static LoginSessionHelper instance;
    private LoginSessionHelper() {}
    public static synchronized LoginSessionHelper getInstance() {
        if (instance == null) {
            instance = new LoginSessionHelper();
        }
        return instance;
    }
    /**
     * Handles login session: ensures key pair exists, stores public key, and syncs with API if needed.
     * @param context Android context
     * @param userId Current user ID (String)
     * @param authToken Authorization token for API requests ("Bearer ...")
     */
    public void handleLogin(Context context, String userId, String authToken) {
        Log.d("LoginSessionHelper", "handleLogin called for userId: " + userId);
        String alias = KeyManager.getAliasForUser(userId);
        try {
            // Check if key exists, generate if not
            if (!KeyManager.hasKey(alias)) {
                Log.d("LoginSessionHelper", "No key found for alias: " + alias + ". Generating new key pair.");
                KeyManager.generateKeyPair(alias);
                Log.d("LoginSessionHelper", "New key pair generated for alias: " + alias);
            } else {
                Log.d("LoginSessionHelper", "Key already exists for alias: " + alias);
            }
            // Get public key and encode
            String publicKeyBase64 = KeyManager.getPublicKeyBase64(KeyManager.getPublicKey(alias));
            String fingerprint = KeyManager.getFingerprint(KeyManager.getPublicKey(alias));

            // Store public key locally
            SQLHelper db = new SQLHelper(context);
            db.insertOrUpdatePublicKey(userId, publicKeyBase64);
            Log.d("LoginSessionHelper", "Public key stored locally for userId: " + userId);

            // Check existing public key on server
            String getUrl = "https://pplugin.works/encryption/get_public_key?q=" + userId;
            com.android.volley.RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(context);
            Log.d("LoginSessionHelper", "Sending GET request to: " + getUrl);
            com.android.volley.toolbox.StringRequest getRequest = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.GET, getUrl,
                response -> {
                    Log.d("LoginSessionHelper", "GET response: " + response);
                    boolean shouldUpload = true;
                    try {
                        if (response != null && !response.isEmpty()) {
                            org.json.JSONObject json = new org.json.JSONObject(response);
                            String serverPublicKey = json.optString("public_key", "");
                            // Compare with local public key
                            shouldUpload = !serverPublicKey.equals(publicKeyBase64);
                            Log.d("LoginSessionHelper", "Server public key: " + serverPublicKey + ", Local public key: " + publicKeyBase64);
                        }
                    } catch (org.json.JSONException e) {
                        Log.e("LoginSessionHelper", "JSON parsing error: ", e);
                        // If response is not valid JSON, treat as should upload
                        shouldUpload = true;
                    }
                    if (shouldUpload) {
                        Log.d("LoginSessionHelper", "Uploading public key to server (POST)");
                        // Upload public key to server
                        String postUrl = "https://pplugin.works/encryption/post_public_key";
                        org.json.JSONObject jsonBody = new org.json.JSONObject();
                        try {
                            jsonBody.put("public_key", publicKeyBase64);
                            jsonBody.put("fingerprint", fingerprint);
                        } catch (org.json.JSONException e) {
                            Log.e("LoginSessionHelper", "JSON error while creating POST body: ", e);
                            return;
                        }
                        com.android.volley.toolbox.JsonObjectRequest postRequest = new com.android.volley.toolbox.JsonObjectRequest(
                            com.android.volley.Request.Method.POST, postUrl, jsonBody,
                            postResponse -> Log.d("LoginSessionHelper", "POST response: " + postResponse),
                            error -> Log.e("LoginSessionHelper", "POST error: ", error)
                        ) {
                            @Override
                            public java.util.Map<String, String> getHeaders() {
                                java.util.Map<String, String> headers = new java.util.HashMap<>();
                                if (authToken != null && !authToken.isEmpty()){
                                    headers.put("Authorization", "Bearer " + authToken);
                                }
                                headers.put("Content-Type", "application/json");
                                return headers;
                            }
                        };
                        queue.add(postRequest);
                    } else {
                        Log.d("LoginSessionHelper", "Public key is up-to-date on server. No upload needed.");
                    }
                },
                error -> {
                    Log.e("LoginSessionHelper", "GET error: ", error);
                    if (error.networkResponse != null) {
                        Log.e("LoginSessionHelper", "GET error status code: " + error.networkResponse.statusCode);
                        if (error.networkResponse.statusCode == 404) {
                            Log.d("LoginSessionHelper", "GET 404: No public key found for user_id, uploading public key.");
                            // Upload public key to server
                            String postUrl = "https://pplugin.works/encryption/post_public_key";
                            org.json.JSONObject jsonBody = new org.json.JSONObject();
                            try {
                                jsonBody.put("public_key", publicKeyBase64);
                                jsonBody.put("fingerprint", fingerprint);
                            } catch (org.json.JSONException e) {
                                Log.e("LoginSessionHelper", "JSON error while creating POST body: ", e);
                                return;
                            }
                            com.android.volley.toolbox.JsonObjectRequest postRequest = new com.android.volley.toolbox.JsonObjectRequest(
                                com.android.volley.Request.Method.POST, postUrl, jsonBody,
                                postResponse -> Log.d("LoginSessionHelper", "POST response: " + postResponse),
                                postError -> Log.e("LoginSessionHelper", "POST error: ", postError)
                            ) {
                                @Override
                                public java.util.Map<String, String> getHeaders() {
                                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                                    if (authToken != null && !authToken.isEmpty()){
                                        headers.put("Authorization", "Bearer " + authToken);
                                    }
                                    headers.put("Content-Type", "application/json");
                                    return headers;
                                }
                            };
                            queue.add(postRequest);
                        }
                        if (error.networkResponse.data != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e("LoginSessionHelper", "GET error response body: " + errorBody);
                        }
                    } else {
                        Log.e("LoginSessionHelper", "GET error: networkResponse is null");
                    }
                }
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    if (authToken != null && !authToken.isEmpty()){
                        headers.put("Authorization", "Bearer " + authToken);
                    }
                    return headers;
                }
            };
            queue.add(getRequest);
        } catch (Exception e) {
            // Handle error (e.g., log, show message)
            Log.e("LoginSessionHelper", "Exception in handleLogin: ", e);
        }
    }
}

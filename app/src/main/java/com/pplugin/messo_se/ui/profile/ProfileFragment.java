package com.pplugin.messo_se.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONObject;

import com.pplugin.messo_se.R;
import com.pplugin.messo_se.ui.LoginActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private Uri selectedAvatarUri = null;
    private boolean avatarChanged = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.fragment_profile, container, false);
        //Innit elements
        Button logoutButton = v.findViewById(R.id.logout_button);
        Button saveButton = v.findViewById(R.id.save_button);
        ImageButton settingsButton = v.findViewById(R.id.settings_menu);
        ImageView profilePicture = v.findViewById(R.id.profile_picture);
        TextView usernameTextView = v.findViewById(R.id.user_name);
        EditText fullNameEditText = v.findViewById(R.id.full_name_edit);
        EditText emailEditText = v.findViewById(R.id.email_edit);
        EditText phoneEditText = v.findViewById(R.id.phone_number_edit);
        EditText bioEditText = v.findViewById(R.id.bio_edit);
        EditText dateOfBirthEditText = v.findViewById(R.id.dob_edit);

        // Edit icons
        ImageView editNameIcon = v.findViewById(R.id.edit_full_name);
        ImageView editEmailIcon = v.findViewById(R.id.edit_email);
        ImageView editPhoneIcon = v.findViewById(R.id.edit_phone_number);
        ImageView editBioIcon = v.findViewById(R.id.edit_bio);
        ImageView editDobIcon = v.findViewById(R.id.edit_dob);

        // Make EditTexts non-editable initially
        EditText[] editTexts = {fullNameEditText, emailEditText, phoneEditText, bioEditText, dateOfBirthEditText};
        for (EditText et : editTexts) {
            et.setFocusable(false);
            et.setFocusableInTouchMode(false);
            et.setClickable(false);
            et.setEnabled(false);
            // Add listener to hide keyboard and unfocus on "Enter"
            et.setOnEditorActionListener((v1, actionId, event) -> {
                v1.clearFocus();
                v1.setFocusable(false);
                v1.setFocusableInTouchMode(false);
                v1.setClickable(false);
                v1.setEnabled(false);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v1.getWindowToken(), 0);
                return true;
            });
        }

        // Helper to enable editing for a single EditText
        View.OnClickListener makeEditable = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText target = null;
                if (view == editNameIcon) target = fullNameEditText;
                else if (view == editEmailIcon) target = emailEditText;
                else if (view == editPhoneIcon) target = phoneEditText;
                else if (view == editBioIcon) target = bioEditText;
                else if (view == editDobIcon) target = dateOfBirthEditText;
                if (target != null) {
                    // Make only this EditText editable
                    for (EditText et : editTexts) {
                        et.setFocusable(false);
                        et.setFocusableInTouchMode(false);
                        et.setClickable(false);
                        et.setEnabled(false);
                    }
                    target.setEnabled(true);
                    target.setFocusable(true);
                    target.setFocusableInTouchMode(true);
                    target.setClickable(true);
                    target.requestFocus();
                    target.setSelection(target.getText().length());
                    // Show keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        };
        editNameIcon.setOnClickListener(makeEditable);
        editEmailIcon.setOnClickListener(makeEditable);
        editPhoneIcon.setOnClickListener(makeEditable);
        editBioIcon.setOnClickListener(makeEditable);
        editDobIcon.setOnClickListener(makeEditable);


        // Volley API call to get profile
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login", getContext().MODE_PRIVATE);
        int id = sharedPreferences.getInt("userId", -1);
        String jwt = sharedPreferences.getString("token", null);
        String username = sharedPreferences.getString("username", null);
        if (username != null) {
            usernameTextView.setText(username);
        } else {
            usernameTextView.setText("Guest");
        }

        if (id != -1 && jwt != null) {
            String url = "http://10.0.2.2:3000/identity/get-profile?id=" + id;
            RequestQueue queue = Volley.newRequestQueue(getContext());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Store the response in a JSONObject
                            JSONObject profileJson = response;
                            // Map response to UI elements if not null
                            try {
                                if (!profileJson.isNull("full_name")) {
                                    fullNameEditText.setText(profileJson.getString("full_name"));
                                }
                                if (!profileJson.isNull("bio")) {
                                    bioEditText.setText(profileJson.getString("bio"));
                                }
                                if (!profileJson.isNull("email")) {
                                    emailEditText.setText(profileJson.getString("email"));
                                }
                                if (!profileJson.isNull("phone")) {
                                    phoneEditText.setText(profileJson.getString("phone"));
                                }
                                if (!profileJson.isNull("date_of_birth")) {
                                    dateOfBirthEditText.setText(profileJson.getString("date_of_birth"));
                                }
                                // Set avatar if avatar_url is not null
                                if (!profileJson.isNull("avatar_url")) {
                                    String avatarUrl = profileJson.getString("avatar_url");
                                    if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals("null")) {
                                        Glide.with(getContext())
                                            .load(avatarUrl)
                                            .placeholder(R.drawable.default_avatar)
                                            .skipMemoryCache(true)
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .into(profilePicture);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("ProfileError", "Error parsing profile: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(), "Error fetching profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("ProfileError", "Error fetching profile: " + error.getMessage());
                        }
                    }
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Authorization", "Bearer " + jwt);
                    return headers;
                }
            };
            queue.add(jsonObjectRequest);
        }

        // Image picker launcher
        ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedAvatarUri = uri;
                    avatarChanged = true;
                    profilePicture.setImageURI(uri);
                }
            }
        );

        // Tap on profile picture to pick image
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageLauncher.launch("image/*");
            }
        });

        // Save button: collect EditText data, send to API, and reload fragment on success
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject editObj = new JSONObject();
                    editObj.put("email", emailEditText.getText().toString());
                    editObj.put("phone", phoneEditText.getText().toString());
                    editObj.put("full_name", fullNameEditText.getText().toString());
                    editObj.put("bio", bioEditText.getText().toString());
                    editObj.put("date_of_birth", dateOfBirthEditText.getText().toString());
                    Log.d("EditObj", editObj.toString());

                    String profileUrl = "http://10.0.2.2:3000/identity/edit-profile";
                    String avatarUrl = "http://10.0.2.2:3000/identity/update-avatar";
                    RequestQueue queue = Volley.newRequestQueue(getContext());

                    // 1. Save profile info
                    JsonObjectRequest postRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        profileUrl,
                        editObj,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("Save", response.toString());
                                Toast.makeText(getContext(), "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                                // 2. If avatar changed, upload avatar
                                if (avatarChanged && selectedAvatarUri != null) {
                                    uploadAvatar(avatarUrl, selectedAvatarUri, queue, jwt);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Save", "Error saving profile: " + error.getMessage());
                                Toast.makeText(getContext(), "Error saving profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    ) {
                        @Override
                        public java.util.Map<String, String> getHeaders() {
                            java.util.Map<String, String> headers = new java.util.HashMap<>();
                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login", getContext().MODE_PRIVATE);
                            String jwt = sharedPreferences.getString("token", null);
                            headers.put("Authorization", "Bearer " + jwt);
                            return headers;
                        }
                    };
                    queue.add(postRequest);

                } catch (Exception e) {
                    Log.e("EditObj", "Error creating JSONObject: " + e.getMessage());
                }
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login", getContext().MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();
                // Go to login activity
                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        return v;
    }

    // Helper to upload avatar using multipart/form-data to /update-avatar
    private void uploadAvatar(String url, Uri imageUri, RequestQueue queue, String jwt) {
        try {
            InputStream iStream = getContext().getContentResolver().openInputStream(imageUri);
            byte[] inputData = getBytes(iStream);
            String extension = getFileExtension(imageUri);
            final String mimeType;
            String detectedMimeType = getContext().getContentResolver().getType(imageUri);
            if (detectedMimeType == null) {
                mimeType = "image/jpeg";
            } else {
                mimeType = detectedMimeType;
            }

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Save", response.toString());
                        Toast.makeText(getContext(), "Avatar updated!", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Save", "Error uploading avatar: " + error.getMessage());
                        Toast.makeText(getContext(), "Error uploading avatar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Authorization", "Bearer " + jwt);
                    return headers;
                }

                @Override
                protected java.util.Map<String, DataPart> getByteData() {
                    java.util.Map<String, DataPart> params = new java.util.HashMap<>();
                    params.put("avatar", new DataPart("avatar." + extension, inputData, mimeType));
                    return params;
                }
            };
            queue.add(multipartRequest);
        } catch (Exception e) {
            Log.e("Save", "Error uploading avatar: " + e.getMessage());
        }
    }

    // Helper to get bytes from InputStream
    private byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    // Helper to get file extension from Uri
    private String getFileExtension(Uri uri) {
        String extension = null;
        if (uri.getScheme().equals("content")) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(getContext().getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new java.io.File(uri.getPath())).toString());
        }
        if (extension == null) extension = "jpg";
        return extension;
    }

    // Minimal DataPart class for multipart
    class DataPart {
        private String fileName;
        private byte[] content;
        private String type;

        public DataPart(String name, byte[] data, String type) {
            this.fileName = name;
            this.content = data;
            this.type = type;
        }

        public String getFileName() { return fileName; }
        public byte[] getContent() { return content; }
        public String getType() { return type; }
    }

    // Minimal VolleyMultipartRequest for multipart/form-data
    class VolleyMultipartRequest extends Request<JSONObject> {
        private final String twoHyphens = "--";
        private final String lineEnd = "\r\n";
        private final String boundary = "apiclient-" + System.currentTimeMillis();

        private Response.Listener<JSONObject> mListener;
        private Response.ErrorListener mErrorListener;
        private Map<String, String> mHeaders = new HashMap<>();

        public VolleyMultipartRequest(int method, String url,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.mListener = listener;
            this.mErrorListener = errorListener;
        }

        @Override
        public String getBodyContentType() {
            return "multipart/form-data;boundary=" + boundary;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return mHeaders != null ? mHeaders : super.getHeaders();
        }

        @Override
        public byte[] getBody() throws AuthFailureError {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                // Text params
                Map<String, String> params = getParams();
                if (params != null && params.size() > 0) {
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        buildTextPart(bos, entry.getKey(), entry.getValue());
                    }
                }
                // File params
                Map<String, DataPart> data = getByteData();
                if (data != null && data.size() > 0) {
                    for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                        buildDataPart(bos, entry.getValue(), entry.getKey());
                    }
                }
                // End boundary
                bos.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
            } catch (IOException e) {
                VolleyLog.e("IOException writing to ByteArrayOutputStream");
            }
            return bos.toByteArray();
        }

        protected Map<String, String> getParams() throws AuthFailureError {
            return null;
        }

        protected Map<String, DataPart> getByteData() throws AuthFailureError {
            return null;
        }

        private void buildTextPart(ByteArrayOutputStream bos, String paramName, String value) throws IOException {
            bos.write((twoHyphens + boundary + lineEnd).getBytes());
            bos.write(("Content-Disposition: form-data; name=\"" + paramName + "\"" + lineEnd).getBytes());
            bos.write((lineEnd).getBytes());
            bos.write((value + lineEnd).getBytes());
        }

        private void buildDataPart(ByteArrayOutputStream bos, DataPart dataFile, String inputName) throws IOException {
            bos.write((twoHyphens + boundary + lineEnd).getBytes());
            bos.write(("Content-Disposition: form-data; name=\"" +
                    inputName + "\"; filename=\"" + dataFile.getFileName() + "\"" + lineEnd).getBytes());
            if (dataFile.getType() != null && !dataFile.getType().trim().isEmpty()) {
                bos.write(("Content-Type: " + dataFile.getType() + lineEnd).getBytes());
            }
            bos.write((lineEnd).getBytes());
            bos.write(dataFile.getContent());
            bos.write((lineEnd).getBytes());
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException | JSONException e) {
                return Response.error(new ParseError(e));
            }
        }

        @Override
        protected void deliverResponse(JSONObject response) {
            mListener.onResponse(response);
        }

        @Override
        public void deliverError(VolleyError error) {
            mErrorListener.onErrorResponse(error);
        }
    }
}

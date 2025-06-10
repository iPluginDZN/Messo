package com.pplugin.messo_se.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.pplugin.messo_se.R;
import android.os.Handler;
import android.os.Looper;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.pplugin.messo_se.adapter.UserSearchAdapter;
import com.pplugin.messo_se.model.UserSearchModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SearchFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        EditText searchEditText = v.findViewById(R.id.edit_search);
        ImageView searchIcon = v.findViewById(R.id.btn_search);
        ListView searchResultsListView = v.findViewById(R.id.list_search_result);
        searchIcon.setOnClickListener(view -> performSearch(searchEditText, searchResultsListView));

        // Handle long clicks on individual items in the search results
        searchResultsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            if (item instanceof UserSearchModel) {
                UserSearchModel user = (UserSearchModel) item;
                Context context = getContext();
                if (context == null) return true;
                SharedPreferences sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
                String jwt = sharedPreferences.getString("token", null);
                if (jwt == null) {
                    Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show();
                    return true;
                }
                String url = "http://10.0.2.2:3000/connections/add-contact";
                JSONObject body = new JSONObject();
                try {
                    // Ensure receiver_id is an integer
                    body.put("receiver_id", Integer.parseInt(user.getUserId()));
                    Log.d("SearchFragment", "Sending request to: " + url + " with body: " + body.toString());
                } catch (Exception e) {
                    Toast.makeText(context, "Error preparing request", Toast.LENGTH_SHORT).show();
                    return true;
                }
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST, url, body,
                        new Response.Listener<org.json.JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Toast.makeText(context, "Request sent", Toast.LENGTH_SHORT).show();
                                // Update UI: set status to pending and refresh the item
                                user.setStatus("pending");
                                // Find the ListView and notify its adapter
                                ListView listView = getView() != null ? getView().findViewById(R.id.list_search_result) : null;
                                if (listView != null && listView.getAdapter() instanceof UserSearchAdapter) {
                                    UserSearchAdapter adapter = (UserSearchAdapter) listView.getAdapter();
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context, "Failed to send request", Toast.LENGTH_SHORT).show();
                                String errorMsg = (error.getMessage() != null) ? error.getMessage() : "null";
                                String errorBody = "";
                                if (error.networkResponse != null && error.networkResponse.data != null) {
                                    errorBody = new String(error.networkResponse.data);
                                }
                                Log.e("SearchFragment", "Error sending request: " + errorMsg + ", status: " +
                                        (error.networkResponse != null ? error.networkResponse.statusCode : "null") +
                                        ", body: " + errorBody);
                            }
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + jwt);
                        return headers;
                    }
                };
                com.android.volley.RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(context);
                queue.add(request);
            } else {
                Toast.makeText(getContext(), "Long clicked item at position " + position, Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        // Set the confirm key (IME_ACTION_SEARCH or IME_ACTION_DONE) to trigger search and hide keyboard
        searchEditText.setOnEditorActionListener((v1, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                performSearch(searchEditText, searchResultsListView);
                // Hide the keyboard
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
        return v;
    }

    private boolean isValidInput(String input) {
        // Username: alphanumeric, 3-20 chars
        Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
        // Email
        boolean isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches();
        // Phone (simple, 10-15 digits)
        Pattern phonePattern = Pattern.compile("^\\+?[0-9]{10,15}$");
        return usernamePattern.matcher(input).matches() || isEmail || phonePattern.matcher(input).matches();
    }
    public void onAddClickTest(View view) {
        android.widget.Toast.makeText(getContext(), "Simple OnClick works!", android.widget.Toast.LENGTH_SHORT).show();
    }

    // Extracted search logic to a method for reuse
    private void performSearch(EditText searchEditText, ListView searchResultsListView) {
        String query = searchEditText.getText().toString().trim();
        if (!isValidInput(query)) {
            Toast.makeText(getContext(), "Invalid input. Enter username, email, or phone.", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("login", getContext().MODE_PRIVATE);
        String jwt = sharedPreferences.getString("token", null);
        String url = "http://10.0.2.2:3000/users/search-users?q=" + query;
        RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(requireContext());
        com.android.volley.toolbox.JsonArrayRequest jsonArrayRequest = new com.android.volley.toolbox.JsonArrayRequest(com.android.volley.Request.Method.GET, url, null,
            new com.android.volley.Response.Listener<org.json.JSONArray>() {
                @Override
                public void onResponse(org.json.JSONArray response) {
                    ArrayList<UserSearchModel> users = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            org.json.JSONObject obj = response.getJSONObject(i);
                            String userId = String.valueOf(obj.optInt("userid"));
                            String userName = obj.optString("username", "");
                            String phone = obj.optString("phone", null);
                            String avatarUrl = obj.optString("avatar_url", null);
                            String status = obj.optString("status", ""); // get status from response
                            users.add(new UserSearchModel(userId, userName, phone, avatarUrl, status));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    UserSearchAdapter adapter = new UserSearchAdapter(getContext(), R.layout.item_user_search, users);
                    searchResultsListView.setAdapter(adapter);
                }
            },
            new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(com.android.volley.VolleyError error) {
                    Toast.makeText(getContext(), "Error searching users", Toast.LENGTH_SHORT).show();
                }
            }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                if (jwt != null) headers.put("Authorization", "Bearer " + jwt);
                return headers;
            }
        };
        queue.add(jsonArrayRequest);
    }
}

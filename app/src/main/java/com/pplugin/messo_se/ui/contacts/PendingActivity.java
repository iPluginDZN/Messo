package com.pplugin.messo_se.ui.contacts;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pplugin.messo_se.R;
import com.pplugin.messo_se.adapter.PendingAdapter;
import com.pplugin.messo_se.model.UserSearchModel;

import org.json.JSONObject;

import java.util.ArrayList;

public class PendingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pending_activity);
        ImageView backButton = findViewById(R.id.backButton);
        TextView pendingCount = findViewById(R.id.pendingCount);
        ListView pendingListView = findViewById(R.id.pendingContactsListView);
        ArrayList<UserSearchModel> pendingContacts = getIntent().getParcelableArrayListExtra("pendingContacts");
        if (pendingContacts != null) {
            pendingCount.setText(String.valueOf(pendingContacts.size()) + " Pending Contacts");
            PendingAdapter adapter = new PendingAdapter(this, R.layout.item_pending, pendingContacts);
            pendingListView.setAdapter(adapter);
            pendingListView.setOnItemClickListener((parent, view, position, id) -> {
                UserSearchModel selectedPendingContact = (UserSearchModel) adapter.getItem(position);
                view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(120).start();
                view.setForeground(getResources().getDrawable(R.drawable.contact_item_darken, null));
                PopupMenu popup = new android.widget.PopupMenu(PendingActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.pending_options, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    // TODO: Handle menu item clicks for selectedContact
                    // Example:
                    if (item.getItemId() == R.id.action_accept) {
                        Integer fromId = null;
                        try {
                            fromId = Integer.parseInt(selectedPendingContact.getUserId());
                            Log.d("PendingActivity", "Accepting contact from ID: " + fromId);
                            setStatus(fromId, "accepted");
                        } catch (NumberFormatException e) {
                            Toast.makeText(PendingActivity.this, "Invalid user ID", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                });
                popup.setOnDismissListener(dialog -> {
                    // Restore the view to original scale and remove darken overlay when menu is dismissed
                    view.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                    view.setForeground(null);
                });
                popup.show();
                // Handle item click if needed, e.g., show user details or options
            });
        } else {
            pendingCount.setText("0");
        }

        backButton.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
    }
    public void setStatus(Integer fromId, String status){
        if (!"accepted".equals(status) && !"rejected".equals(status)) return;
        Context context = this;
        SharedPreferences sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
        String jwt = sharedPreferences.getString("token", null);
        String url = "https://pplugin.works/connections/set-contact-status";
        RequestQueue queue = Volley.newRequestQueue(context);
        try {
            if (fromId == null || status == null) {
                Log.e("PendingActivity", "fromId or status is null. Not sending request.");
                Toast.makeText(context, "Invalid request data", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONObject body = new JSONObject();
            body.put("request_from_id", fromId);
            body.put("status", status);
            Log.d("PendingActivity", "Request body: " + body.toString());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    Toast.makeText(context, "Contact added", Toast.LENGTH_SHORT).show();
                    // Remove the contact from the list and update the adapter
                    runOnUiThread(() -> {
                        ListView listView = findViewById(R.id.pendingContactsListView);
                        PendingAdapter adapter = (PendingAdapter) listView.getAdapter();
                        for (int i = 0; i < adapter.getCount(); i++) {
                            UserSearchModel user = (UserSearchModel) adapter.getItem(i);
                            if (user.getUserId().equals(String.valueOf(fromId))) {
                                adapter.getPendingList().remove(i);
                                adapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    });
                },
                error -> {
                    Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show();
                    Log.e("PendingActivity", "Volley error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("PendingActivity", "Status code: " + error.networkResponse.statusCode);
                        if (error.networkResponse.data != null) {
                            String responseBody = new String(error.networkResponse.data);
                            Log.e("PendingActivity", "Response body: " + responseBody);
                        }
                    }
                }) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Content-Type", "application/json; utf-8");
                    if (jwt != null) headers.put("Authorization", "Bearer " + jwt);
                    return headers;
                }
            };
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}

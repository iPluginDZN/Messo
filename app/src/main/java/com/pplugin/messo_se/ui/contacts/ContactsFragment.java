package com.pplugin.messo_se.ui.contacts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.pplugin.messo_se.R;
import com.pplugin.messo_se.adapter.ContactListAdapter;
import com.pplugin.messo_se.model.ContactModel;
import com.pplugin.messo_se.model.UserSearchModel;
import com.pplugin.messo_se.ui.messages.MessageActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContactsFragment extends Fragment {
    private ArrayList<UserSearchModel> pendingContacts = new ArrayList<>();
    private ActivityResultLauncher<Intent> pendingActivityLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.fragment_contacts, container, false);
        // Initialize views and set up any necessary listeners here
        ImageView pendingRequests = v.findViewById(R.id.pendingRequestIcon);
        TextView pendingRequestsCount = v.findViewById(R.id.pendingRequestBadge);
        ListView contactListView = v.findViewById(R.id.contacts_result);

        // Register the activity result launcher
        pendingActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    // Reload contacts when returning from PendingActivity
                    getContact(contactListView);
                    getPendingContact(new PendingContactCallback() {
                        @Override
                        public void onPendingContactsLoaded(ArrayList<UserSearchModel> result) {
                            pendingContacts.clear();
                            pendingContacts.addAll(result);
                            pendingRequestsCount.setText(String.valueOf(pendingContacts.size()));
                            pendingRequestsCount.setVisibility(pendingContacts.isEmpty() ? View.GONE : View.VISIBLE);
                        }
                    });
                }
            }
        );

        getContact(contactListView);
        getPendingContact(new PendingContactCallback() {
            @Override
            public void onPendingContactsLoaded(ArrayList<UserSearchModel> result) {
                pendingContacts.clear();
                pendingContacts.addAll(result);
                pendingRequestsCount.setText(String.valueOf(pendingContacts.size()));
                pendingRequestsCount.setVisibility(pendingContacts.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        pendingRequests.setOnClickListener(v1 -> {
            Intent intent = new Intent(getContext(), PendingActivity.class);
            intent.putParcelableArrayListExtra("pendingContacts", pendingContacts);
            pendingActivityLauncher.launch(intent);
        });

        return v;
    }

    private void getContact(ListView contactListView) {
        Context context = getContext();
        if (context == null) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
        String jwt = sharedPreferences.getString("token", null);
        String url = "https://pplugin.works/connections/get-contacts";
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
            response -> {
                ArrayList<ContactModel> contacts = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject obj = response.getJSONObject(i);
                        String userId = String.valueOf(obj.optInt("user_id"));
                        String userName = obj.optString("user_name", "");
                        String phone = obj.optString("phone", null);
                        String fullName = obj.optString("full_name", "");
                        String avatarUrl = obj.optString("avatar_url", null);
                        contacts.add(new ContactModel(userId, userName, fullName, phone, avatarUrl));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ContactListAdapter adapter = new ContactListAdapter(context, R.layout.item_contacts, contacts);
                contactListView.setAdapter(adapter);

                // Add click listener to show contact options menu
                contactListView.setOnItemClickListener((parent, view, position, id) -> {
                    ContactModel selectedContact = (ContactModel) adapter.getItem(position);
                    // Animate the view to indicate press (scale up slightly)
                    view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(120).start();
                    view.setForeground(getResources().getDrawable(R.drawable.contact_item_darken, null));
                    PopupMenu popup = new android.widget.PopupMenu(context, view);
                    popup.getMenuInflater().inflate(R.menu.contact_options_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(item -> {
                        // TODO: Handle menu item clicks for selectedContact
                        // Example:
                        // if (item.getItemId() == R.id.action_see_profile) { ... }
                        if (item.getItemId() == R.id.action_message) {
                            Intent intent = new Intent(context, MessageActivity.class);
                            intent.putExtra("userId", selectedContact.getUserId());
                            intent.putExtra("userName", selectedContact.getUserName());
                            intent.putExtra("avatarUrl", selectedContact.getAvatarUrl());
                            context.startActivity(intent);
                        }
                        return true;
                    });
                    popup.setOnDismissListener(dialog -> {
                        // Restore the view to original scale and remove darken overlay when menu is dismissed
                        view.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                        view.setForeground(null);
                    });
                    popup.show();
                });
            },
            error -> Toast.makeText(context, "Error fetching contacts", Toast.LENGTH_SHORT).show()
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

    // Fetches the list of pending contacts from the API and stores them in an ArrayList<UserSearchModel>
    private void getPendingContact(PendingContactCallback callback) {
        Context context = getContext();
        if (context == null) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
        String jwt = sharedPreferences.getString("token", null);
        String url = "https://pplugin.works/connections/get-pending-contacts";
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
            response -> {
                ArrayList<UserSearchModel> pendingContacts = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        org.json.JSONObject obj = response.getJSONObject(i);
                        String userId = String.valueOf(obj.optInt("user_id"));
                        String userName = obj.optString("user_name", "");
                        String phone = obj.optString("phone", "");
                        String avatarUrl = obj.optString("avatar_url", null);
                        String status = obj.optString("status", null);
                        pendingContacts.add(new UserSearchModel(userId, userName, phone, avatarUrl, status));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (callback != null) callback.onPendingContactsLoaded(pendingContacts);
            },
            error -> {
                if (callback != null) callback.onPendingContactsLoaded(new ArrayList<>());
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

    // Callback interface for async result
    public interface PendingContactCallback {
        void onPendingContactsLoaded(ArrayList<com.pplugin.messo_se.model.UserSearchModel> pendingContacts);
    }
}

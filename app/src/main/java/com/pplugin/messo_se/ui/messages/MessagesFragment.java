package com.pplugin.messo_se.ui.messages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pplugin.messo_se.R;
import com.pplugin.messo_se.adapter.MessageListAdapter;
import com.pplugin.messo_se.model.Conversation;
import com.pplugin.messo_se.sqllite.SQLHelper;

import org.json.JSONObject;

import java.util.List;

public class MessagesFragment extends Fragment {
    private MessageListAdapter adapter;
    private List<Conversation> conversations;
    private ListView listView;
    private BroadcastReceiver messageReceiver;
    private String userId;
    private SQLHelper sqlHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_messages, container, false);
        listView = v.findViewById(R.id.list_search_result);
        sqlHelper = new SQLHelper(requireContext());
        // Get logged-in userId from SharedPreferences
        android.content.SharedPreferences sharedPreferences = requireContext().getSharedPreferences("login", android.content.Context.MODE_PRIVATE);
        userId = String.valueOf(sharedPreferences.getInt("userId", -1));
        loadConversations();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Conversation conversation = conversations.get(position);
                Intent intent = new Intent(requireContext(), MessageActivity.class);
                intent.putExtra("userId", conversation.getUserId());
                intent.putExtra("userName", conversation.getUsername());
                intent.putExtra("avatarUrl", conversation.getAvatarUrl());
                startActivity(intent);
            }
        });
        return v;
    }

    private void loadConversations() {
        conversations = sqlHelper.getConversations(userId);
        if (adapter == null) {
            adapter = new MessageListAdapter(requireContext(), conversations);
            listView.setAdapter(adapter);
        } else {
            adapter.setConversations(conversations);
            adapter.notifyDataSetChanged();
        }
        for (Conversation conversation : conversations) {
            fetchAndSetUserInfo(conversation, adapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload conversations when returning to this fragment
        loadConversations();
        // Register broadcast receiver for new messages
        if (messageReceiver == null) {
            messageReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // Reload conversations when a new message is received
                    loadConversations();
                }
            };
        }
        IntentFilter filter = new IntentFilter("com.pplugin.messo_se.NEW_MESSAGE");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(messageReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (messageReceiver != null) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver);
        }
    }

    private void fetchAndSetUserInfo(Conversation conversation, MessageListAdapter adapter) {
        String url = "https://pplugin.works/users/search-user-id?id=" + conversation.getUserId();
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        // Get JWT from SharedPreferences
        android.content.SharedPreferences sharedPreferences = requireContext().getSharedPreferences("login", android.content.Context.MODE_PRIVATE);
        String jwt = sharedPreferences.getString("token", null);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    String username = response.optString("username", conversation.getUserId());
                    String avatarUrl = response.optString("avatar_url", null);
                    conversation.setUsername(username);
                    conversation.setAvatarUrl(avatarUrl);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    // Ignore, fallback to userId
                }
            },
            error -> {
                // Ignore error, fallback to userId
            }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                if (jwt != null) headers.put("Authorization", "Bearer " + jwt);
                return headers;
            }
        };
        queue.add(request);
    }
}

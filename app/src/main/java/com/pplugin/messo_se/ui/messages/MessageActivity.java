package com.pplugin.messo_se.ui.messages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pplugin.messo_se.R;
import com.pplugin.messo_se.sqllite.SQLHelper;
import com.pplugin.messo_se.utils.KeyManager;
import com.pplugin.messo_se.webrtc.SocketIOManager;

import org.json.JSONObject;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    private TextView recipientName;
    private String userId;
    private String recipientId;
    private String avatarUrl;

    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();
    private SQLHelper sqlHelper;

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");
            String message = intent.getStringExtra("message");
            if (from != null && from.equals(recipientId) && message != null) {
                // Do NOT store incoming message in local DB here (already stored by ChatService)
                // Just update the UI
                messageList.add(new Message(message, from, Message.Type.INCOMING));
                messageAdapter.notifyItemInserted(messageList.size() - 1);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());
        recipientName = findViewById(R.id.text_person_name);

        // Get userId from SharedPreferences
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        userId = String.valueOf(sharedPreferences.getInt("userId", -1));
        // Get recipientId from Intent
        recipientId = getIntent().getStringExtra("userId");
        avatarUrl = getIntent().getStringExtra("avatarUrl");
        recipientName.setText(getIntent().getStringExtra("userName"));
        // SocketIOManager is now managed by ChatService, do not connect here
        // socketIOManager = new SocketIOManager("https://pplugin.works", userId, this);
        // socketIOManager.connect();

        // Setup RecyclerView for messages
        RecyclerView recyclerView = findViewById(R.id.recycler_messages);
        messageAdapter = new MessageAdapter(this, messageList, userId, avatarUrl);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        // Wire up send button
        EditText editMessage = findViewById(R.id.edit_message);
        ImageButton btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> {
            String message = editMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                editMessage.setText("");
            }
        });

        sqlHelper = new SQLHelper(this);
        // Load message history from DB
        List<Message> history = sqlHelper.getMessages(userId, recipientId);
        messageList.addAll(history);
        messageAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.pplugin.messo_se.NEW_MESSAGE");
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Do not disconnect SocketIOManager here, handled by ChatService
        // if (socketIOManager != null) {
        //     socketIOManager.disconnect();
        // }
    }

    // Example method to send a message (call this from your send button)
    private void sendMessage(String message) {
        // Encrypt message before sending
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        String jwt = sharedPreferences.getString("token", null);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://pplugin.works/encryption/get_public_key?q=" + recipientId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    String recipientPublicKeyBase64 = response.optString("public_key", null);
                    String fingerprint = response.optString("fingerprint", null);
                    if (recipientPublicKeyBase64 == null || fingerprint == null) {
                        runOnUiThread(() -> Toast.makeText(this, "Recipient public key not found", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    // Verify fingerprint
                    java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(android.util.Base64.decode(recipientPublicKeyBase64, android.util.Base64.NO_WRAP));
                    java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
                    PublicKey recipientPublicKey = keyFactory.generatePublic(keySpec);
                    String calculatedFingerprint = KeyManager.getFingerprint(recipientPublicKey);
                    if (!fingerprint.equalsIgnoreCase(calculatedFingerprint)) {
                        runOnUiThread(() -> Toast.makeText(this, "Public key fingerprint mismatch!", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    String encryptedMessage = KeyManager.encryptWithPublicKey(message, recipientPublicKey);
                    // Send encrypted message via ChatService
                    Intent serviceIntent = new Intent(this, com.pplugin.messo_se.services.ChatService.class);
                    serviceIntent.putExtra("sendTo", recipientId);
                    serviceIntent.putExtra("encryptedMessage", encryptedMessage);
                    startService(serviceIntent);
                    // Store outgoing message in DB as plain text
                    long now = System.currentTimeMillis();
                    sqlHelper.insertMessage(userId, recipientId, message, "OUTGOING", now);
                    // Only update the UI for outgoing message
                    messageList.add(new Message(message, userId, Message.Type.OUTGOING));
                    runOnUiThread(() -> messageAdapter.notifyItemInserted(messageList.size() - 1));
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "Encryption error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            },
            error -> runOnUiThread(() -> Toast.makeText(this, "Failed to get recipient public key", Toast.LENGTH_SHORT).show())
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

package com.pplugin.messo_se.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pplugin.messo_se.R;
import com.pplugin.messo_se.sqllite.SQLHelper;
import com.pplugin.messo_se.ui.messages.MessageActivity;
import com.pplugin.messo_se.utils.KeyManager;
import com.pplugin.messo_se.webrtc.SocketIOManager;
import org.json.JSONObject;

import java.security.PrivateKey;

public class ChatService extends Service implements SocketIOManager.SocketIOListener {
    public static final String CHANNEL_ID = "ChatServiceChannel";
    private SocketIOManager socketIOManager;
    private String userId;
    private SQLHelper sqlHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        sqlHelper = new SQLHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Always get userId from intent or SharedPreferences
        userId = intent.getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            android.content.SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
            userId = String.valueOf(sharedPreferences.getInt("userId", -1));
        }
        if (userId != null && !userId.equals("-1") && socketIOManager == null) {
            socketIOManager = new SocketIOManager("https://pplugin.works", userId, this);
            socketIOManager.connect();
        }
        // Handle send message intent
        String sendTo = intent.getStringExtra("sendTo");
        String encryptedMessage = intent.getStringExtra("encryptedMessage");
        if (sendTo != null && encryptedMessage != null && socketIOManager != null) {
            // Only send the message, do not store outgoing message in DB here
            socketIOManager.sendMessage(sendTo, encryptedMessage);
        }
        createNotificationChannel();
        Notification notification = buildNotification("");
        startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (socketIOManager != null) {
            socketIOManager.disconnect();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected() {
        Log.d("ChatService", "Connected to chat server");
    }

    @Override
    public void onDisconnected() {
        Log.d("ChatService", "Disconnected from chat server");
    }

    @Override
    public void onMessageReceived(String from, String message) {
        try {
            String alias = KeyManager.getAliasForUser(userId);
            PrivateKey privateKey = KeyManager.getPrivateKey(alias);
            String decryptedMessage = KeyManager.decryptWithPrivateKey(message, privateKey);
            long now = System.currentTimeMillis();
            sqlHelper.insertMessage(from, userId, decryptedMessage, "INCOMING", now);
            showNotification(from, decryptedMessage);
            // Send local broadcast for real-time UI update
            Intent intent = new Intent("com.pplugin.messo_se.NEW_MESSAGE");
            intent.putExtra("from", from);
            intent.putExtra("message", decryptedMessage);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } catch (Exception e) {
            Log.e("ChatService", "Decryption error", e);
        }
    }

    @Override
    public void onError(String error) {
        Log.e("ChatService", "Socket error: " + error);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chat Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification buildNotification(String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Messo App")
                .setContentText("Application is running!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Messo App"))
                .setSmallIcon(R.drawable.default_avatar)
                .build();
    }

    private void showNotification(String from, String message) {
        // Fetch username and avatar_url from backend before showing notification
        String url = "https://pplugin.works/users/search-user-id?id=" + from;
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        String jwt = sharedPreferences.getString("token", null);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    String username = response.optString("username", from);
                    String avatarUrl = response.optString("avatar_url", null);
                    Intent intent = new Intent(this, MessageActivity.class);
                    intent.putExtra("userId", from);
                    if (avatarUrl != null) intent.putExtra("avatarUrl", avatarUrl);
                    if (username != null) intent.putExtra("userName", username);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setContentTitle("New message from " + username)
                            .setContentText(message)
                            .setSmallIcon(R.drawable.default_avatar)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);
                    Notification notification = builder.build();
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify((int) System.currentTimeMillis(), notification);
                },
                error -> {
                    // Fallback: show notification with userId if request fails
                    Intent intent = new Intent(this, MessageActivity.class);
                    intent.putExtra("userId", from);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setContentTitle("New message from " + from)
                            .setContentText(message)
                            .setSmallIcon(R.drawable.default_avatar)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build();
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify((int) System.currentTimeMillis(), notification);
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

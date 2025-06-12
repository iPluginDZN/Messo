package com.pplugin.messo_se.webrtc;

import android.util.Log;
import org.json.JSONObject;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketIOManager {
    public interface SocketIOListener {
        void onConnected();
        void onDisconnected();
        void onMessageReceived(String from, String message);
        void onError(String error);
    }

    private Socket socket;
    private SocketIOListener listener;
    private String userId;

    public SocketIOManager(String serverUrl, String userId, SocketIOListener listener) {
        this.listener = listener;
        this.userId = userId;
        try {
            IO.Options options = new IO.Options();
            // You can set options here if needed (e.g., auth)
            socket = IO.socket(serverUrl, options);
        } catch (URISyntaxException e) {
            if (listener != null) listener.onError(e.getMessage());
        }
        setupListeners();
    }

    private void setupListeners() {
        if (socket == null) return;
        socket.on(Socket.EVENT_CONNECT, args -> {
            if (listener != null) listener.onConnected();
            // Register userId with the server
            socket.emit("register", userId);
        });
        socket.on(Socket.EVENT_DISCONNECT, args -> {
            if (listener != null) listener.onDisconnected();
        });
        socket.on("message", args -> {
            if (args.length > 0 && args[0] instanceof JSONObject) {
                JSONObject obj = (JSONObject) args[0];
                String from = obj.optString("from");
                String message = obj.optString("message");
                if (listener != null) listener.onMessageReceived(from, message);
            }
        });
        socket.on("connect_error", args -> {
            Log.e("SocketIOManager", "EVENT_CONNECT_ERROR: " + (args.length > 0 ? args[0] : "no args"));
            if (listener != null) listener.onError("Connect error: " + (args.length > 0 ? args[0] : "no args"));
        });
        socket.on("error", args -> {
            Log.e("SocketIOManager", "EVENT_ERROR: " + (args.length > 0 ? args[0] : "no args"));
            if (listener != null) listener.onError("Socket error: " + (args.length > 0 ? args[0] : "no args"));
        });
        socket.on("connect_timeout", args -> {
            Log.e("SocketIOManager", "EVENT_CONNECT_TIMEOUT: " + (args.length > 0 ? args[0] : "no args"));
            if (listener != null) listener.onError("Connect timeout: " + (args.length > 0 ? args[0] : "no args"));
        });
        socket.on("reconnect", args -> {
            Log.i("SocketIOManager", "EVENT_RECONNECT: " + (args.length > 0 ? args[0] : "no args"));
        });
        socket.on("reconnect_error", args -> {
            Log.e("SocketIOManager", "EVENT_RECONNECT_ERROR: " + (args.length > 0 ? args[0] : "no args"));
        });
        socket.on("reconnect_failed", args -> {
            Log.e("SocketIOManager", "EVENT_RECONNECT_FAILED: " + (args.length > 0 ? args[0] : "no args"));
        });
        socket.on("ping", args -> {
            Log.d("SocketIOManager", "EVENT_PING");
        });
        socket.on("pong", args -> {
            Log.d("SocketIOManager", "EVENT_PONG");
        });
    }

    public void connect() {
        if (socket != null) socket.connect();
    }

    public void disconnect() {
        if (socket != null) socket.disconnect();
    }

    public void sendMessage(String to, String message) {
        if (socket != null && socket.connected()) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("to", to);
                obj.put("from", userId);
                obj.put("message", message);
                socket.emit("message", obj);
            } catch (Exception e) {
                if (listener != null) listener.onError(e.getMessage());
            }
        }
    }
}

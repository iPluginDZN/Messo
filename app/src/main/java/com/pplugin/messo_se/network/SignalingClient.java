package com.pplugin.messo_se.network;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class SignalingClient extends WebSocketClient {
    public interface Callback {
        void onOfferReceived(String sdp);
        void onAnswerReceived(String sdp);
        void onIceCandidateReceived(String candidateJson);
        void onConnected();
        void onDisconnected();
        void onError(Exception ex);
    }

    private Callback callback;

    public SignalingClient(String serverUri, Map<String, String> httpHeaders, Callback callback) throws URISyntaxException {
        super(new URI(serverUri), httpHeaders);
        this.callback = callback;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        if (callback != null) callback.onConnected();
    }

    @Override
    public void onMessage(String message) {
        // Simple protocol: {"type":"offer/answer/candidate", "data":"..."}
        try {
            org.json.JSONObject obj = new org.json.JSONObject(message);
            String type = obj.getString("type");
            String data = obj.getString("data");
            if ("offer".equals(type)) {
                callback.onOfferReceived(data);
            } else if ("answer".equals(type)) {
                callback.onAnswerReceived(data);
            } else if ("candidate".equals(type)) {
                callback.onIceCandidateReceived(data);
            }
        } catch (Exception e) {
            if (callback != null) callback.onError(e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (callback != null) callback.onDisconnected();
    }

    @Override
    public void onError(Exception ex) {
        if (callback != null) callback.onError(ex);
    }

    public void sendOffer(String sdp) {
        sendMessage("offer", sdp);
    }

    public void sendAnswer(String sdp) {
        sendMessage("answer", sdp);
    }

    public void sendCandidate(String candidateJson) {
        sendMessage("candidate", candidateJson);
    }

    public void sendOffer(String sdp, String to, String from) {
        sendMessage("offer", sdp, to, from);
    }

    public void sendAnswer(String sdp, String to, String from) {
        sendMessage("answer", sdp, to, from);
    }

    public void sendCandidate(String candidateJson, String to, String from) {
        sendMessage("candidate", candidateJson, to, from);
    }

    private void sendMessage(String type, String data) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("type", type);
            obj.put("data", data);
            send(obj.toString());
        } catch (Exception e) {
            if (callback != null) callback.onError(e);
        }
    }

    private void sendMessage(String type, String data, String to, String from) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("type", type);
            obj.put("data", data);
            obj.put("to", to);
            obj.put("from", from);
            send(obj.toString());
        } catch (Exception e) {
            if (callback != null) callback.onError(e);
        }
    }
}

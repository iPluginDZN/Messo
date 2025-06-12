package com.pplugin.messo_se.model;

public class Conversation {
    private String userId;
    private String username;
    private String avatarUrl;
    private String latestMessage;
    private long timestamp;

    public Conversation(String userId, String username, String avatarUrl, String latestMessage, long timestamp) {
        this.userId = userId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.latestMessage = latestMessage;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getLatestMessage() { return latestMessage; }
    public long getTimestamp() { return timestamp; }
    public void setUsername(String username) { this.username = username; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}

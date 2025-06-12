package com.pplugin.messo_se.ui.messages;

public class Message {
    public enum Type { INCOMING, OUTGOING }
    private final String text;
    private final String senderId;
    private final Type type;
    public Message(String text, String senderId, Type type) {
        this.text = text;
        this.senderId = senderId;
        this.type = type;
    }
    public String getText() { return text; }
    public String getSenderId() { return senderId; }
    public Type getType() { return type; }
}


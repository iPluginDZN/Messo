package com.pplugin.messo_se.model;

public class ContactModel {
    private String userId;
    private String userName;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private boolean isOnline = false;

    public ContactModel() {
    }

    public ContactModel(String userId, String userName, String fullName, String phone, String avatarUrl) {
        this.userId = userId;
        this.userName = userName;
        this.fullName = fullName;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
    }

    public ContactModel(String userId, String userName, String fullName, String phone, String avatarUrl, boolean status) {
        this.userId = userId;
        this.userName = userName;
        this.fullName = fullName;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.isOnline = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    public boolean isOnline() {
        return isOnline;
    }
    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}

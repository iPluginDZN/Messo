package com.pplugin.messo_se.model;

public class UserSearchModel {
    private String userId;
    private String userName;
    private String phone;
    private String avatarUrl;
    private String status;
    public UserSearchModel() {
    }
    public UserSearchModel(String userId, String userName, String fullName, String avatarUrl) {
        this.userId = userId;
        this.userName = userName;
        this.phone = fullName;
        this.avatarUrl = avatarUrl;
    }
    public UserSearchModel(String userId, String userName, String fullName, String avatarUrl, String status) {
        this.userId = userId;
        this.userName = userName;
        this.phone = fullName;
        this.avatarUrl = avatarUrl;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

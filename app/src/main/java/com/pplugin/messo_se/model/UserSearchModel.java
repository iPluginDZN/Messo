package com.pplugin.messo_se.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserSearchModel implements Parcelable {
    private String userId;
    private String userName;
    private String phone;
    private String avatarUrl;
    private String status;
    public UserSearchModel() {
    }
    public UserSearchModel(String userId, String userName, String phone, String avatarUrl) {
        this.userId = userId;
        this.userName = userName;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
    }
    public UserSearchModel(String userId, String userName, String phone, String avatarUrl, String status) {
        this.userId = userId;
        this.userName = userName;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.status = status;
    }

    protected UserSearchModel(Parcel in) {
        userId = in.readString();
        userName = in.readString();
        phone = in.readString();
        avatarUrl = in.readString();
        status = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(phone);
        dest.writeString(avatarUrl);
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<UserSearchModel> CREATOR = new Parcelable.Creator<UserSearchModel>() {
        @Override
        public UserSearchModel createFromParcel(Parcel in) {
            return new UserSearchModel(in);
        }

        @Override
        public UserSearchModel[] newArray(int size) {
            return new UserSearchModel[size];
        }
    };

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

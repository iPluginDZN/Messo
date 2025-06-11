package com.pplugin.messo_se.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pplugin.messo_se.R;
import com.pplugin.messo_se.model.UserSearchModel;

import java.util.List;

public class PendingAdapter extends BaseAdapter {
    Context context;
    int layout;
    List<UserSearchModel> pendingList;
    public PendingAdapter(Context context, int layout, List<UserSearchModel> pendingList) {
        this.context = context;
        this.layout = layout;
        this.pendingList = pendingList;
    }
    @Override
    public int getCount() {
        return pendingList.size();
    }
    @Override
    public Object getItem(int position) {
        return pendingList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(layout, parent, false);
            }
            UserSearchModel user = pendingList.get(position);
            ImageView profileImage = convertView.findViewById(R.id.img_profile);
            TextView userName = convertView.findViewById(R.id.tv_username);
            TextView phone = convertView.findViewById(R.id.tv_phone);
            TextView pendingText = convertView.findViewById(R.id.tv_pending);
            // Set the pending text to indicate the user is pending
            pendingText.setText("Pending");
            pendingText.setVisibility(View.VISIBLE);
            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl == null || avatarUrl.isEmpty() || "null".equalsIgnoreCase(avatarUrl)) {
                profileImage.setImageResource(R.drawable.default_avatar);
            } else {
                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .fallback(R.drawable.default_avatar)
                        .into(profileImage);
            }
            // Bind user data to views
            if (user.getUserName() != null && !user.getUserName().isEmpty()) {
                userName.setText(user.getUserName());
            } else {
                userName.setText("Unknown");
            }
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                phone.setText(user.getPhone());
            } else {
                phone.setText("");
            }
            return convertView;
        } catch (Exception e) {
            e.printStackTrace();
            return convertView;
        }
    }

    public List<UserSearchModel> getPendingList() {
        return pendingList;
    }
}

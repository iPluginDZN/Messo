package com.pplugin.messo_se.adapter;

import com.pplugin.messo_se.R;
import com.pplugin.messo_se.model.UserSearchModel;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class UserSearchAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<UserSearchModel> userList;
    public UserSearchAdapter(Context context, int layout, List<UserSearchModel> userList) {
        this.context = context;
        this.layout = layout;
        this.userList = userList;
    }
    @Override
    public int getCount() {
        return userList.size();
    }
    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    // Add callback interface for Add button
    public interface OnAddClickListener {
        void onAddClick(UserSearchModel user);
    }
    private OnAddClickListener addClickListener;
    public void setOnAddClickListener(OnAddClickListener listener) {
        this.addClickListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(layout, parent, false);
            }

            UserSearchModel user = userList.get(position);
            ImageView profileImage = convertView.findViewById(R.id.img_profile);
            TextView userName = convertView.findViewById(R.id.tv_username);
            TextView phone = convertView.findViewById(R.id.tv_phone);
            TextView holdToAdd = convertView.findViewById(R.id.hold_to_add);
            TextView pendingText = convertView.findViewById(R.id.tv_pending);


//            Log.d("UserSearchAdapter", "getView position: " + position + ", user: " + user.getUserName());
//            Log.d("UserSearchAdapter", "addContactButton: " + addContactButton + ", pendingText: " + pendingText);

            Log.d("AvatarURL", "User: " + user.getUserName() + ", Avatar URL: " + user.getAvatarUrl());
            // Set default_avatar directly if user.getAvatarUrl() is null, empty, or the string 'null'
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
            userName.setText(user.getUserName());
            phone.setText(user.getPhone());


            if (user.getStatus() == null || user.getStatus().isEmpty()) {
                holdToAdd.setVisibility(View.VISIBLE);
                pendingText.setVisibility(View.GONE);

            } else if ("pending".equalsIgnoreCase(user.getStatus())) {
                holdToAdd.setVisibility(View.GONE);
                pendingText.setVisibility(View.VISIBLE);
                pendingText.setText("Pending");
                pendingText.setTextColor(context.getResources().getColor(R.color.gray));
            } else if ("accepted".equalsIgnoreCase(user.getStatus())) {
                holdToAdd.setVisibility(View.GONE);
                pendingText.setVisibility(View.GONE);
            } else {
                holdToAdd.setVisibility(View.VISIBLE);
                holdToAdd.setEnabled(true);
                pendingText.setVisibility(View.GONE);
            }
            return convertView;
        } catch (Exception e) {
            Log.e("UserSearchAdapter", "Exception in getView", e);
            return convertView;
        }
    }
}

package com.pplugin.messo_se.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.content.Context;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import com.bumptech.glide.Glide;
import com.pplugin.messo_se.R;
import com.pplugin.messo_se.model.ContactModel;


public class ContactListAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<ContactModel> contactList;

    public ContactListAdapter(Context context, int layout, List<ContactModel> contactList) {
        this.context = context;
        this.layout = layout;
        this.contactList = contactList;
    }

    @Override
    public int getCount() {
        return contactList.size();
    }
    @Override
    public Object getItem(int position) {
        return contactList.get(position);
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
            ImageView profileImage = convertView.findViewById(R.id.img_profile);
            TextView userName = convertView.findViewById(R.id.tv_username);
            TextView fullName = convertView.findViewById(R.id.tv_fullname);
            TextView phone = convertView.findViewById(R.id.tv_phone);
            TextView onlineStatus = convertView.findViewById(R.id.tv_online_status);

            ContactModel contact = contactList.get(position);

            String avatarUrl = contact.getAvatarUrl();
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
            userName.setText(contact.getUserName());
            fullName.setText(contact.getFullName());
            phone.setText(contact.getPhone());

            // Set online status text and color
            if (contact.isOnline()) {
                onlineStatus.setText("Online");
                onlineStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else {
                onlineStatus.setText("Offline");
                onlineStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }

            return convertView;
        } catch (Exception e) {
            e.printStackTrace();
            return convertView; // Return the original view in case of an error
        }
    }
}

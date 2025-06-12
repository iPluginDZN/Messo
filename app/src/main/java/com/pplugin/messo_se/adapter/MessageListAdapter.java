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
import com.pplugin.messo_se.model.Conversation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageListAdapter extends BaseAdapter {
    private Context context;
    private List<Conversation> conversationList;

    public MessageListAdapter(Context context, List<Conversation> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
    }

    @Override
    public int getCount() {
        return conversationList.size();
    }

    @Override
    public Object getItem(int position) {
        return conversationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_messages, parent, false);
        }
        ImageView imageProfile = convertView.findViewById(R.id.image_profile);
        TextView textUsername = convertView.findViewById(R.id.text_username);
        TextView textLatestMessage = convertView.findViewById(R.id.text_latest_message);
        TextView textTimestamp = convertView.findViewById(R.id.text_timestamp);

        Conversation conversation = conversationList.get(position);
        textUsername.setText(conversation.getUsername());
        textLatestMessage.setText(conversation.getLatestMessage());
        // Format timestamp
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(conversation.getTimestamp()));
        textTimestamp.setText(time);
        // Load avatar
        if (conversation.getAvatarUrl() != null && !conversation.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                .load(conversation.getAvatarUrl())
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(imageProfile);
        } else {
            imageProfile.setImageResource(R.drawable.default_avatar);
        }
        return convertView;
    }
    public void setConversations(List<Conversation> conversations) {
        this.conversationList = conversations;
        notifyDataSetChanged();
    }
    public List<Conversation> getConversations() {
        return conversationList;
    }
}

package com.pplugin.messo_se.ui.messages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.pplugin.messo_se.R;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_OUTGOING = 1;
    private static final int VIEW_TYPE_INCOMING = 2;
    private final List<Message> messages;
    private final String currentUserId;
    private final Context context;
    private final String avatarUrl;

    public MessageAdapter(Context context, List<Message> messages, String currentUserId, String avatarUrl) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.avatarUrl = avatarUrl;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getType() == Message.Type.OUTGOING ? VIEW_TYPE_OUTGOING : VIEW_TYPE_INCOMING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_OUTGOING) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_outgoing, parent, false);
            return new OutgoingViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_incoming, parent, false);
            return new IncomingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof OutgoingViewHolder) {
            ((OutgoingViewHolder) holder).bind(message);
        } else if (holder instanceof IncomingViewHolder) {
            ((IncomingViewHolder) holder).bind(message, avatarUrl, context);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class OutgoingViewHolder extends RecyclerView.ViewHolder {
        private final TextView textMessageBody;
        OutgoingViewHolder(View itemView) {
            super(itemView);
            textMessageBody = itemView.findViewById(R.id.text_message_body);
        }
        void bind(Message message) {
            textMessageBody.setText(message.getText());
        }
    }

    static class IncomingViewHolder extends RecyclerView.ViewHolder {
        private final TextView textMessageBody;
        private final ImageView imageProfile;
        IncomingViewHolder(View itemView) {
            super(itemView);
            textMessageBody = itemView.findViewById(R.id.text_message_body);
            imageProfile = itemView.findViewById(R.id.image_profile);
        }
        void bind(Message message, String avatarUrl, Context context) {
            textMessageBody.setText(message.getText());
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(context).load(avatarUrl).placeholder(R.drawable.default_avatar).into(imageProfile);
            } else {
                imageProfile.setImageResource(R.drawable.default_avatar);
            }
        }
    }
}

package com.example.projectmxh.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.Model.Message;
import com.example.projectmxh.R;


import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<Message> messages;
    private final String currentUser;

    public GroupChatAdapter(List<Message> messages, String currentUser) {
        this.messages = messages;
        this.currentUser = currentUser;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getSenderName().equals(currentUser) ?
                VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @Override
    public RecyclerView.@NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_groupchat, parent, false));
        } else {
            return new ReceivedMessageViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_groupchatmember, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.myMessageText);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText, senderName;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.otherMessageText);
            senderName = itemView.findViewById(R.id.otherUserName);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
            senderName.setText(message.getSenderName());
        }
    }
}

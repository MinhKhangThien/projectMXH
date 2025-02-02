package com.example.projectmxh.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmxh.R;
import com.example.projectmxh.Model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private final List<User> users;
    private final OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public MessageAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.onUserClickListener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView avatar;
        private final TextView senderName;
        private final TextView latestMessage;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            senderName = itemView.findViewById(R.id.senderName);
            latestMessage = itemView.findViewById(R.id.latestMessage);
        }

        void bind(User user) {
            senderName.setText(user.getDisplayName());
            latestMessage.setText("Last message..."); // Có thể update message cuối cùng từ server
            Glide.with(itemView.getContext())
                    .load("https://i.pinimg.com/474x/b7/14/a2/b714a2713d5d9259dab2a7c0b7df4ff9.jpg")
                    .placeholder(R.drawable.ic_avatar)
                    .into(avatar);

            itemView.setOnClickListener(v -> onUserClickListener.onUserClick(user));
        }
    }
}

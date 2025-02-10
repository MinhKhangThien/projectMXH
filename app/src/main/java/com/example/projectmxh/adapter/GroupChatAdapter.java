package com.example.projectmxh.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmxh.Model.Message;
import com.example.projectmxh.R;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;


import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final Context context;

    private final List<Message> messages;
    private String currentUser;

    public GroupChatAdapter(Context context, List<Message> messages, String currentUser) {
        this.context = context;
        this.messages = messages;
        this.currentUser = currentUser;
    }


    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        boolean isSentByMe = message.getSenderName().equals(currentUser);
        Log.d("GroupChatAdapter", String.format("Message sender: %s, current user: %s, is sent by me: %b",
                message.getSenderName(), currentUser, isSentByMe));
        return isSentByMe ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
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
        Log.d("GroupChatAdapter", String.format("Binding message - sender: %s, current user: %s",
                message.getSenderName(), currentUser));

        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            loadSenderInfo(message.getSenderName(), (ReceivedMessageViewHolder) holder, message);
        }
    }

    private void loadSenderInfo(String username, ReceivedMessageViewHolder holder, Message message) {
        ApiService apiService = ApiClient.getClientWithToken(context).create(ApiService.class);
        apiService.getUserByEmail(username).enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    holder.bind(message, response.body());
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                holder.bind(message, null);
            }
        });
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
        private final CircleImageView senderAvatar;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.otherMessageText);
            senderName = itemView.findViewById(R.id.otherUserName);
            senderAvatar = itemView.findViewById(R.id.otherUserAvatar);
        }

        void bind(Message message, AppUserDto sender) {
            messageText.setText(message.getMessage());
            if (sender != null) {
                senderName.setText(sender.getDisplayName());
                if (sender.getProfilePicture() != null) {
                    Glide.with(itemView.getContext())
                            .load(sender.getProfilePicture())
                            .placeholder(R.drawable.avatar)
                            .error(R.drawable.avatar)
                            .circleCrop()
                            .into(senderAvatar);
                }
            } else {
                senderName.setText(message.getSenderName());
                senderAvatar.setImageResource(R.drawable.avatar);
            }
        }
    }

    public void updateCurrentUser(String currentUser) {
        this.currentUser = currentUser;
        notifyDataSetChanged();
    }
}

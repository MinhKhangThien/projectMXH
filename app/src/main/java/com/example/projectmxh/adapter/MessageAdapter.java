package com.example.projectmxh.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.projectmxh.Model.ChatBox;
import com.example.projectmxh.R;
import com.example.projectmxh.screen.ChatActivity;
import com.example.projectmxh.screen.GroupChatActivity;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final String TAG = "MessageAdapter";
    private final List<ChatBox> chatBoxes;

    public MessageAdapter(List<ChatBox> chatBoxes) {
        this.chatBoxes = chatBoxes;
        Log.d(TAG, "MessageAdapter created with " + chatBoxes.size() + " chat boxes");
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatBox chatBox = chatBoxes.get(position);
        if (chatBox == null) {
            Log.e("MessageAdapter", "Null chat box at position " + position);
            return;
        }
        Log.d(TAG, "Binding chat box at position " + position +
                " for user: " + chatBox.getUsername());
        holder.bind(chatBox);

        holder.itemView.setOnClickListener(v -> {
            if (chatBox.isGroup()) {
                if (chatBox.getId() == null) {
                    Log.e("MessageAdapter", "Group chat has null ID");
                    Toast.makeText(v.getContext(),
                            "Invalid group chat", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(v.getContext(), GroupChatActivity.class);
                intent.putExtra("groupId", chatBox.getId());
                intent.putExtra("groupName", chatBox.getName());
                intent.putExtra("groupImage", chatBox.getImage());
                Log.d("MessageAdapter", "Opening group chat: " + chatBox.getName());
                Log.d("MessageAdapter", "Opening group chat: " + chatBox.getId());

                v.getContext().startActivity(intent);
            } else {
                if (chatBox.getUsername() == null) {
                    Log.e("MessageAdapter", "Private chat has null username");
                    Toast.makeText(v.getContext(),
                            "Invalid chat", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                intent.putExtra("receiverId", chatBox.getId());
                intent.putExtra("receiverName", chatBox.getUsername());
                intent.putExtra("receiverFullName", chatBox.getName());
                intent.putExtra("avatarUrl", chatBox.getImage());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatBoxes.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView avatar;
        private final TextView name;
        private final TextView lastMessage;
        private final ImageView groupIndicator;
        private TextView unreadCount;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.senderName);
            lastMessage = itemView.findViewById(R.id.latestMessage);
            groupIndicator = itemView.findViewById(R.id.groupIndicator);
            unreadCount = itemView.findViewById(R.id.unreadCount);
        }

        void bind(ChatBox chatBox) {
            name.setText(chatBox.getName());
            lastMessage.setText(chatBox.getLastMessage());
            groupIndicator.setVisibility(chatBox.isGroup() ? View.VISIBLE : View.GONE);

            // Load image with error handling
            if (chatBox.getImage() != null && !chatBox.getImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(chatBox.getImage())
                        .placeholder(chatBox.isGroup() ? R.drawable.ic_groupchat : R.drawable.avatar)
                        .error(chatBox.isGroup() ? R.drawable.ic_groupchat : R.drawable.avatar)
                        .into(avatar);
            } else {
                // Set default image
                avatar.setImageResource(chatBox.isGroup() ? R.drawable.ic_groupchat : R.drawable.avatar);
            }

            // Show unread count if greater than 0
            if (chatBox.getUnreadCount() > 0) {
                unreadCount.setVisibility(View.VISIBLE);
                unreadCount.setText(String.valueOf(chatBox.getUnreadCount()));
            } else {
                unreadCount.setVisibility(View.GONE);
            }
        }
    }
}
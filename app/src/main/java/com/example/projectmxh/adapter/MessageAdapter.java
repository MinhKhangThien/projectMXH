package com.example.projectmxh.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
    private final List<ChatBox> chatBoxes;

    public MessageAdapter(List<ChatBox> chatBoxes) {
        this.chatBoxes = chatBoxes;
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
        holder.bind(chatBox);

        holder.itemView.setOnClickListener(v -> {
            if (chatBox.isGroup()) {
                Intent intent = new Intent(v.getContext(), GroupChatActivity.class);
                intent.putExtra("groupId", chatBox.getId());
                v.getContext().startActivity(intent);
            } else {
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
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

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.senderName);
            lastMessage = itemView.findViewById(R.id.latestMessage);
            groupIndicator = itemView.findViewById(R.id.groupIndicator);
        }

        void bind(ChatBox chatBox) {
            name.setText(chatBox.getName());
            lastMessage.setText(chatBox.getLastMessage());
            groupIndicator.setVisibility(chatBox.isGroup() ? View.VISIBLE : View.GONE);

            int defaultImage = chatBox.isGroup() ?
                    R.drawable.ic_groupchat :
                    R.drawable.avatar;

            Glide.with(itemView.getContext())
                    .load(chatBox.getImage())
                    .placeholder(defaultImage)
                    .into(avatar);
        }
    }
}
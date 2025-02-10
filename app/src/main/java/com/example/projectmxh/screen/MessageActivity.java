package com.example.projectmxh.screen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.BaseActivity;
import com.example.projectmxh.Components.MessageReadReceiver;
import com.example.projectmxh.Model.ChatBox;
import com.example.projectmxh.Model.ChatList;
import com.example.projectmxh.Model.GroupChat;
import com.example.projectmxh.R;
import com.example.projectmxh.adapter.MessageAdapter;
import com.example.projectmxh.Model.User;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.dto.request.PendingFollowRequest;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity implements MessageReadReceiver.MessageReadCallback{
    private RecyclerView messagesList;
    private MessageAdapter messageAdapter;
    private List<ChatBox> chatBoxes;
    private ImageView messageIcon;
    private ImageView logoIcon;
    private BroadcastReceiver messageReadReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages);


        messagesList = findViewById(R.id.messages_list);
        chatBoxes = new ArrayList<>();

        messageAdapter = new MessageAdapter(chatBoxes);
        messagesList.setLayoutManager(new LinearLayoutManager(this));
        messagesList.setAdapter(messageAdapter);

        messageIcon = findViewById(R.id.messageIcon);
        messageIcon.setOnClickListener(v -> showPopupMenu(v));

        logoIcon = findViewById(R.id.logoIcon);
        logoIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MessageActivity.this, BaseActivity.class);
            startActivity(intent);
        });

        loadBoxChats();

        registerMessageReceiver();
    }

    private void registerMessageReceiver() {
        IntentFilter intentFilter = new IntentFilter("MESSAGES_READ");
        messageReadReceiver = new MessageReadReceiver(this);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(messageReadReceiver, intentFilter);
    }

    private void loadBoxChats() {
        chatBoxes.clear(); // Clear existing chats

        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getChatList().enqueue(new Callback<List<ChatList>>() {
            @Override
            public void onResponse(Call<List<ChatList>> call, Response<List<ChatList>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Create a HashSet to track unique usernames
                    Set<String> addedUsernames = new HashSet<>();

                    for (ChatList chat : response.body()) {
                        AppUserDto contact = chat.getContact();
                        if (contact != null && contact.getUsername() != null &&
                                !addedUsernames.contains(contact.getUsername())) {

                            ChatBox chatBox = new ChatBox(
                                    contact.getId(),
                                    contact.getDisplayName(),
                                    contact.getUsername(),
                                    contact.getProfilePicture(),
                                    false,
                                    chat.getLastMessage()
                            );
                            chatBox.setUnreadCount(chat.getUnreadCount());
                            chatBoxes.add(chatBox);

                            // Add username to tracked set
                            addedUsernames.add(contact.getUsername());
                            Log.d("MessageActivity", "Added chat for user: " + contact.getUsername());
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                    // Load group chats after private chats are loaded
                    loadGroupChats();
                }
            }

            @Override
            public void onFailure(Call<List<ChatList>> call, Throwable t) {
                Log.e("MessageActivity", "Failed to load chat list", t);
                Toast.makeText(MessageActivity.this,
                        "Error loading chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupChats() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getGroupChats().enqueue(new Callback<List<GroupChat>>() {
            @Override
            public void onResponse(Call<List<GroupChat>> call, Response<List<GroupChat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("MessageActivity", "Loaded " + response.body().size() + " group chats");
                    for (GroupChat group : response.body()) {
                        if (group != null && group.getId() != null) {
                            // Check if group chat already exists using safe null checks
                            boolean exists = false;
                            for (ChatBox existing : chatBoxes) {
                                if (existing != null &&
                                        existing.getId() != null &&
                                        existing.getId().equals(group.getId())) {
                                    exists = true;
                                    break;
                                }
                            }

                            if (!exists) {
                                ChatBox chatBox = new ChatBox(
                                        group.getId(),
                                        group.getName() != null ? group.getName() : "Unnamed Group",
                                        null,
                                        group.getImage() != null ? group.getImage() : "",
                                        true,
                                        "Group Chat"
                                );
                                chatBoxes.add(chatBox);
                                Log.d("MessageActivity", "Added group chat: " + chatBox.getName());
                            }
                        } else {
                            Log.w("MessageActivity", "Received null or invalid group chat data");
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                } else {
                    try {
                        Log.e("MessageActivity", "Failed to load group chats: " +
                                (response.errorBody() != null ? response.errorBody().string() : "Unknown error"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GroupChat>> call, Throwable t) {
                Log.e("MessageActivity", "Network error loading group chats", t);
                Toast.makeText(MessageActivity.this,
                        "Error loading group chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFollowings(String userId) {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getFollowings(userId).enqueue(new Callback<List<PendingFollowRequest>>() {
            @Override
            public void onResponse(Call<List<PendingFollowRequest>> call, Response<List<PendingFollowRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (PendingFollowRequest following : response.body()) {
                        chatBoxes.add(new ChatBox(
                                following.getId().toString(),
                                following.getFullName(),
                                following.getUserName(),
                                following.getProfilePicture(),
                                false,
                                "Click to start chatting"
                        ));
                    }
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<PendingFollowRequest>> call, Throwable t) {
                Log.e("MessageActivity", "Failed to load followings: " + t.getMessage());
                Toast.makeText(MessageActivity.this, "Error loading chat list", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPopupMenu(View view) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.message_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.create_group) {
                startActivity(new Intent(this, CreateGroupChatActivity.class));
                return true;
            }
            return false;
        });

        popup.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh chat list when activity resumes
        loadBoxChats();
    }

    private void updateUnreadCount(String contactUsername) {
        for (int i = 0; i < chatBoxes.size(); i++) {
            ChatBox chatBox = chatBoxes.get(i);
            if (chatBox.getUsername() != null &&
                    chatBox.getUsername().equals(contactUsername)) {
                chatBox.setUnreadCount(0);
                messageAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageReadReceiver != null) {
            LocalBroadcastManager.getInstance(this)
                    .unregisterReceiver(messageReadReceiver);
        }
    }

    @Override
    public void onMessageRead(String contactUsername) {
        updateUnreadCount(contactUsername);
    }


}

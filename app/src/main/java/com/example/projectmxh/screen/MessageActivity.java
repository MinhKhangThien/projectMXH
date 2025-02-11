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

    private boolean isInitialLoad = true;

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

        // Initial load of chats
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
        chatBoxes.clear();
        Log.d("MessageActivity", "Cleared existing chat boxes. Size: " + chatBoxes.size());

        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getChatList().enqueue(new Callback<List<ChatList>>() {
            @Override
            public void onResponse(Call<List<ChatList>> call, Response<List<ChatList>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Set<String> addedUsernames = new HashSet<>();

                    for (ChatList chat : response.body()) {
                        AppUserDto contact = chat.getContact();
                        if (contact != null && contact.getUsername() != null &&
                                !addedUsernames.contains(contact.getUsername())) {

                            ChatBox chatBox = new ChatBox(
                                    null,
                                    contact.getDisplayName(),
                                    contact.getUsername(),
                                    contact.getProfilePicture(),
                                    false,
                                    chat.getLastMessage()
                            );
                            chatBox.setUnreadCount(chat.getUnreadCount());
                            chatBoxes.add(chatBox);
                            addedUsernames.add(contact.getUsername());
                            Log.d("MessageActivity", "Added chat box for: " + contact.getUsername());
                        }
                    }
                    // Only load group chats after private chats are loaded
                    loadGroupChats();
                } else {
                    Log.e("MessageActivity", "Failed to load chat list: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ChatList>> call, Throwable t) {
                Log.e("MessageActivity", "Failed to load chat list", t);
            }
        });
    }

// Remove the loadFollowings method since it's no longer needed

    private void loadGroupChats() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getGroupChats().enqueue(new Callback<List<GroupChat>>() {
            @Override
            public void onResponse(Call<List<GroupChat>> call, Response<List<GroupChat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Set<String> addedGroupIds = new HashSet<>();

                    for (GroupChat group : response.body()) {
                        if (group != null && group.getId() != null &&
                                !addedGroupIds.contains(group.getId())) {

                            ChatBox chatBox = new ChatBox(
                                    group.getId(),
                                    group.getName() != null ? group.getName() : "Unnamed Group",
                                    null,
                                    group.getImage() != null ? group.getImage() : "",
                                    true,
                                    "Group Chat"
                            );
                            chatBoxes.add(chatBox);
                            addedGroupIds.add(group.getId());
                            Log.d("MessageActivity", "Added group chat: " + chatBox.getName());
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<GroupChat>> call, Throwable t) {
                Log.e("MessageActivity", "Network error loading group chats", t);
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
        if (!isInitialLoad) {
            loadBoxChats(); // Only reload if not initial load
        }
        isInitialLoad = false;
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

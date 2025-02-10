package com.example.projectmxh.screen;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.projectmxh.Model.GroupChat;
import com.example.projectmxh.Model.Message;
import com.example.projectmxh.Model.Status;
import com.example.projectmxh.R;
import com.example.projectmxh.adapter.GroupChatAdapter;
import com.example.projectmxh.config.WebSocketConfig;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;
import com.google.gson.Gson;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectmxh.R;
import com.example.projectmxh.adapter.GroupChatAdapter;
import com.example.projectmxh.Model.Message;
import com.example.projectmxh.config.WebSocketConfig;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;
import com.google.gson.Gson;
import de.hdodenhof.circleimageview.CircleImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupChatActivity extends AppCompatActivity implements WebSocketConfig.WebSocketMessageListener {
    private static final String TAG = "GroupChatActivity";

    // View fields
    private RecyclerView groupChatMessages;
    private EditText messageEditText;
    private ImageView sendIcon, backIcon, photoIcon, groupSettings;
    private CircleImageView groupAvatar;
    private TextView groupName, groupStatus;
    private LinearLayout messageInput;

    private WebSocketConfig webSocket;
    private GroupChatAdapter adapter;
    private List<Message> messages;
    private String groupId;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.groupchat);

        String groupId = getIntent().getStringExtra("groupId");
        String groupName = getIntent().getStringExtra("groupName");
        String groupImage = getIntent().getStringExtra("groupImage");

        if (groupId == null) {
            Log.e(TAG, "No group ID provided");
            Toast.makeText(this, "Error: Invalid group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        updateInitialUI(groupName, groupImage);
        getCurrentUserAndSetupChat(groupId);
    }

    private void updateInitialUI(String name, String image) {
        if (name != null) {
            groupName.setText(name);
        }

        if (image != null && !image.isEmpty()) {
            Glide.with(this)
                    .load(image)
                    .placeholder(R.drawable.ic_groupchat)
                    .error(R.drawable.ic_groupchat)
                    .into(groupAvatar);
        } else {
            groupAvatar.setImageResource(R.drawable.ic_groupchat);
        }
    }

    private void initViews() {
        // Initialize views with proper IDs from layout
        groupChatMessages = findViewById(R.id.groupChatMessages);
        messageEditText = findViewById(R.id.messageEditText);
        sendIcon = findViewById(R.id.sendIcon);
        backIcon = findViewById(R.id.backIcon);
        photoIcon = findViewById(R.id.photoIcon);
        groupSettings = findViewById(R.id.groupSettings);
        groupAvatar = findViewById(R.id.groupAvatar);
        groupName = findViewById(R.id.groupName);
        groupStatus = findViewById(R.id.groupStatus);
        messageInput = findViewById(R.id.messageInput);

        // Initialize RecyclerView
        messages = new ArrayList<>();
        adapter = new GroupChatAdapter(this, messages, currentUser); // Update constructor call
        groupChatMessages.setLayoutManager(new LinearLayoutManager(this));
        groupChatMessages.setAdapter(adapter);

        setupClickListeners();
    }

    private void setupClickListeners() {
        backIcon.setOnClickListener(v -> finish());
        sendIcon.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageEditText.setText("");
            }
        });

        groupSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, GroupChatDetailActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        });
    }

    private void getCurrentUserAndSetupChat(String groupId) {
        this.groupId = groupId;
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body().getUsername();

                    // Initialize adapter with currentUser
                    adapter = new GroupChatAdapter(GroupChatActivity.this, messages, currentUser);
                    groupChatMessages.setLayoutManager(new LinearLayoutManager(GroupChatActivity.this));
                    groupChatMessages.setAdapter(adapter);

                    setupWebSocket();
                    loadGroupMessages(groupId);
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                Log.e(TAG, "Failed to get current user", t);
                Toast.makeText(GroupChatActivity.this,
                        "Failed to initialize chat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupMembers(String groupId) {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getGroupChatUsers(groupId).enqueue(new Callback<List<AppUserDto>>() {
            @Override
            public void onResponse(Call<List<AppUserDto>> call, Response<List<AppUserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateGroupInfo(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<AppUserDto>> call, Throwable t) {
                Log.e(TAG, "Failed to load group members", t);
            }
        });
    }

    private void updateGroupInfo(List<AppUserDto> members) {
        if (members != null) {
            int memberCount = members.size();
            String statusText = memberCount + (memberCount == 1 ? " member" : " members");
            groupStatus.setText(statusText);
        }
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            RecyclerView messagesRecyclerView = findViewById(R.id.groupChatMessages);
            messagesRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }



    private void setupWebSocket() {
        webSocket = new WebSocketConfig(this);
        if (currentUser != null && groupId != null) {
            webSocket.connectAndSubscribeToGroup(currentUser, groupId);
        }
    }

    @Override
    public void handleNewMessage(String messageJson) {
        try {
            Message message = new Gson().fromJson(messageJson, Message.class);

            // Check if message belongs to this group chat
            if (message.getReceiverName().equals(groupId)) {
                runOnUiThread(() -> {
                    // Add message to local list
                    messages.add(message);
                    adapter.notifyItemInserted(messages.size() - 1);
                    scrollToBottom();

                    // Log for debugging
                    Log.d(TAG, "Added message: " + message.getMessage() +
                            " from: " + message.getSenderName());
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling new message", e);
        }
    }

    public String getGroupId() {
        return groupId;
    }

    private void sendMessage(String content) {
        if (currentUser == null || groupId == null) {
            Log.e(TAG, "Cannot send message: currentUser or groupId is null");
            return;
        }

        Message message = new Message();
        message.setSenderName(currentUser);
        message.setReceiverName(groupId); // This should be the group ID
        message.setMessage(content);
        message.setStatus(Status.MESSAGE);
        message.setDate(new Date().toString());

        String jsonMessage = new Gson().toJson(message);

        webSocket.sendGroupMessage("/group-message/" + groupId, jsonMessage);

        messageEditText.setText("");
    }

    private void loadGroupMessages(String groupId) {
        if (groupId == null) {
            Log.e(TAG, "Group ID is null");
            return;
        }

        Log.d(TAG, "Loading messages for group: " + groupId);
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getGroupChatMessages(UUID.fromString(groupId))
                .enqueue(new Callback<List<Message>>() {
                    @Override
                    public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            messages.clear();
                            messages.addAll(response.body());
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "Loaded " + messages.size() + " messages");
                            scrollToBottom();

                            // Load member info for messages
                            for (Message message : messages) {
                                loadSenderInfo(message.getSenderName());
                            }
                        } else {
                            Log.e(TAG, "Failed to load messages: " + response.code());
                            if (response.errorBody() != null) {
                                try {
                                    Log.e(TAG, "Error body: " + response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            Toast.makeText(GroupChatActivity.this,
                                    "Failed to load messages", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Message>> call, Throwable t) {
                        Log.e(TAG, "Network error loading messages", t);
                        Toast.makeText(GroupChatActivity.this,
                                "Network error loading messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadSenderInfo(String username) {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getUserByEmail(username).enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cache user info if needed
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                Log.e(TAG, "Failed to load sender info: " + username, t);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close();
        }
    }
}

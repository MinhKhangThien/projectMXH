package com.example.projectmxh.screen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmxh.Model.Status;
import com.example.projectmxh.R;
import com.example.projectmxh.adapter.ChatAdapter;
import com.example.projectmxh.Model.Message;
import com.example.projectmxh.config.WebSocketConfig;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity implements WebSocketConfig.WebSocketMessageListener{
    private WebSocketConfig webSocket;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private String currentUser;
    private String receiverUser, receiverId;

    // UI Elements
    private RecyclerView chatMessages;
    private EditText messageEditText;
    private ImageView sendIcon, backIcon, detailIcon;
    private CircleImageView avatar;
    private TextView userName;

    private BroadcastReceiver blockStatusReceiver;
    private boolean isBlockedByUser = false;
    private View messageInputLayout;
    private View blockedMessageLayout;

    private boolean hasBlockedUser = false;
    private View youBlockedMessageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        initializeViews();
        setupClickListeners();
        // Get receiverEmail first
        String receiverEmail = getIntent().getStringExtra("receiverName");
        if (receiverEmail != null) {
            // Fetch user details using email
            ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
            apiService.getUserByEmail(receiverEmail).enqueue(new Callback<AppUserDto>() {
                @Override
                public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AppUserDto user = response.body();
                        receiverId = user.getId(); // Now we have the correct ID
                        receiverUser = user.getUsername();

                        // Update UI with user details
                        userName.setText(user.getDisplayName());
                        if (user.getProfilePicture() != null) {
                            Glide.with(ChatActivity.this)
                                    .load(user.getProfilePicture())
                                    .placeholder(R.drawable.ic_avatar)
                                    .into(avatar);
                        }

                        checkBlockStatus();
                        // Now setup chat after getting user details
                        setupChatWithUser();
                    }
                }

                @Override
                public void onFailure(Call<AppUserDto> call, Throwable t) {
                    Log.e("ChatActivity", "Failed to get user details", t);
                    Toast.makeText(ChatActivity.this,
                            "Failed to load user details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

        // Mark messages as read when opening chat
        getCurrentUserAsync(username -> {
            currentUser = username;
            markMessagesAsRead();
        });

        registerBlockStatusReceiver();
    }

    private void setupChatWithUser() {
        messages = new ArrayList<>();
        getCurrentUserAsync(username -> {
            currentUser = username;
            chatAdapter = new ChatAdapter(messages, currentUser);
            chatMessages.setLayoutManager(new LinearLayoutManager(this));
            chatMessages.setAdapter(chatAdapter);

            webSocket = new WebSocketConfig(this);
            webSocket.connectAndSubscribe(currentUser);
            loadMessages();

            // Check block status after getting user details
            checkIfBlocked();
            Log.d("ChatActivity", "Chat setup complete for user: " + currentUser);
        });
    }

    private void initializeViews() {
        chatMessages = findViewById(R.id.chatMessages);
        messageEditText = findViewById(R.id.messageEditText);
        sendIcon = findViewById(R.id.sendIcon);
        backIcon = findViewById(R.id.backIcon);
        detailIcon = findViewById(R.id.detail);
        avatar = findViewById(R.id.avatar);
        userName = findViewById(R.id.userName);
        ImageView videoCallIcon = findViewById(R.id.videoCallIcon);
        videoCallIcon.setOnClickListener(v -> startVideoCall());
        messageInputLayout = findViewById(R.id.messageInput);
        blockedMessageLayout = findViewById(R.id.blockedMessageLayout);

        messageInputLayout = findViewById(R.id.messageInput);
        blockedMessageLayout = findViewById(R.id.blockedMessageLayout);
        youBlockedMessageLayout = findViewById(R.id.youBlockedMessageLayout);
    }

    private void checkBlockStatus() {
        if (receiverId == null) {
            Log.e("ChatActivity", "Cannot check block status: receiverId is null");
            return;
        }

        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);

        // First check if we're blocked by the other user
        apiService.isUserBlocked(receiverId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isBlockedByUser = response.body();
                    // Then check if we blocked them
                    checkIfWeBlockedUser();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("ChatActivity", "Failed to check if blocked by user", t);
            }
        });
    }

    private void checkIfWeBlockedUser() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);

        apiService.isBlockedUser(receiverId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    hasBlockedUser = response.body();
                    updateBlockUI();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("ChatActivity", "Failed to check if we blocked user", t);
            }
        });
    }

    private void updateBlockUI() {
        runOnUiThread(() -> {
            if (messageInputLayout != null &&
                    blockedMessageLayout != null &&
                    youBlockedMessageLayout != null) {

                if (isBlockedByUser) {
                    // We are blocked by the other user
                    messageInputLayout.setVisibility(View.GONE);
                    blockedMessageLayout.setVisibility(View.VISIBLE);
                    youBlockedMessageLayout.setVisibility(View.GONE);
                } else if (hasBlockedUser) {
                    // We blocked the other user
                    messageInputLayout.setVisibility(View.GONE);
                    blockedMessageLayout.setVisibility(View.GONE);
                    youBlockedMessageLayout.setVisibility(View.VISIBLE);
                } else {
                    // No blocking in either direction
                    messageInputLayout.setVisibility(View.VISIBLE);
                    blockedMessageLayout.setVisibility(View.GONE);
                    youBlockedMessageLayout.setVisibility(View.GONE);
                }

                Log.d("ChatActivity", "Updated UI - Blocked by user: " + isBlockedByUser +
                        ", Has blocked user: " + hasBlockedUser);
            }
        });
    }

    private void setupClickListeners() {
        backIcon.setOnClickListener(v -> navigateToMessageActivity());

        sendIcon.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageEditText.setText("");
            }
        });

        detailIcon.setOnClickListener(v -> {
            if (receiverId != null) {
                Intent intent = new Intent(this, DetailChatActivity.class);
                intent.putExtra("userId", receiverId);
                intent.putExtra("username", receiverUser);
                intent.putExtra("fullname", userName.getText().toString());
                intent.putExtra("avatarUrl", getIntent().getStringExtra("avatarUrl"));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Could not open chat details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMessageActivity() {
        Intent intent = new Intent(ChatActivity.this, MessageActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupChat() {
        receiverId = getIntent().getStringExtra("receiverId");
        Log.d("ChatActivity", "Receiver ID: " + receiverId);
        receiverUser = getIntent().getStringExtra("receiverName");
        String receiverFullname = getIntent().getStringExtra("receiverFullName");
        String avatarUrl = getIntent().getStringExtra("avatarUrl");

        userName.setText(receiverFullname);
        Glide.with(this).load(avatarUrl).placeholder(R.drawable.ic_avatar).into(avatar);

        messages = new ArrayList<>();

        // Get current user first, then setup adapter and WebSocket
        getCurrentUserAsync(username -> {
            currentUser = username;
            chatAdapter = new ChatAdapter(messages, currentUser);
            chatMessages.setLayoutManager(new LinearLayoutManager(this));
            chatMessages.setAdapter(chatAdapter);

            // Initialize WebSocket after we have currentUser
            webSocket = new WebSocketConfig(this);
            webSocket.connectAndSubscribe(currentUser); // Add this line
            loadMessages();
        });
    }

    private void getCurrentUserAsync(OnUserLoadedCallback callback) {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String username = response.body().getUsername();
                    Log.d("ChatActivity", "Current user loaded: " + username);
                    callback.onUserLoaded(username);
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                Log.e("ChatActivity", "Failed to get current user: " + t.getMessage());
                Toast.makeText(ChatActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getCurrentUser() {
        return currentUser;
    }

    interface OnUserLoadedCallback {
        void onUserLoaded(String username);
    }

    private void loadMessages() {
        if(currentUser == null || currentUser.isEmpty()){
            currentUser = getCurrentUser();
            return;
        }
        Log.d("ChatActivity", "Loading messages for currentUser: " + currentUser + " and receiverUser: " + receiverUser);

        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getPrivateMessages(currentUser, receiverUser)
                .enqueue(new Callback<List<Message>>() {
                    @Override
                    public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                        Log.d("ChatActivity", "Response code: " + response.code());
                        if (response.isSuccessful()) {
                            Log.d("ChatActivity", "Response body: " + new Gson().toJson(response.body()));
                            if (response.body() != null) {
                                messages.clear();
                                messages.addAll(response.body());
                                chatAdapter.notifyDataSetChanged();
                                scrollToBottom();
                                Log.d("ChatActivity", "Loaded " + messages.size() + " messages");
                            }
                        } else {
                            Log.e("ChatActivity", "Error response: " + response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Message>> call, Throwable t) {
                        Log.e("ChatActivity", "API call failed: " + t.getMessage());
                        Toast.makeText(ChatActivity.this,
                                "Failed to load messages: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMessage(String content) {
        Message message = new Message();
        message.setSenderName(currentUser);
        message.setReceiverName(receiverUser);
        message.setMessage(content);
        message.setStatus(Status.MESSAGE);
        message.setDate(new Date().toString());

        // Add message to local list immediately
        messages.add(message);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();

        // Clear input
        messageEditText.setText("");

        // Send via WebSocket
        String jsonMessage = new Gson().toJson(message);
        webSocket.send("/app/private-message", jsonMessage);
    }

    @Override
    public void handleNewMessage(String messageJson) {
        Log.d("ChatActivity", "Received message: " + messageJson);

        try {
            Message message = new Gson().fromJson(messageJson, Message.class);

            // Check if message belongs to this chat
            if ((message.getSenderName().equals(currentUser) &&
                    message.getReceiverName().equals(receiverUser)) ||
                    (message.getSenderName().equals(receiverUser) &&
                            message.getReceiverName().equals(currentUser))) {

                runOnUiThread(() -> {
                    messages.add(message);
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                    scrollToBottom();

                    // Mark as read if we're the receiver
                    if (message.getSenderName().equals(receiverUser)) {
                        markMessagesAsRead();
                    }
                });
            }
        } catch (Exception e) {
            Log.e("ChatActivity", "Error parsing message", e);
        }
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            chatMessages.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    private void registerBlockStatusReceiver() {
        blockStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("USER_BLOCKED".equals(intent.getAction())) {
                    String blockedUserId = intent.getStringExtra("blockedUserId");
                    Log.d("ChatActivity", "Received block broadcast for userId: " + blockedUserId);
                    if (blockedUserId != null && blockedUserId.equals(receiverId)) {
                        Log.d("ChatActivity", "Updating UI for blocked user");
                        updateBlockedUI(true);
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(blockStatusReceiver, new IntentFilter("USER_BLOCKED"));
        Log.d("ChatActivity", "Block status receiver registered");
    }

    private void checkIfBlocked() {
        if (receiverId == null) {
            Log.e("ChatActivity", "Cannot check block status: receiverId is null");
            return;
        }

        Log.d("ChatActivity", "Checking block status for receiverId: " + receiverId);
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.isUserBlocked(receiverId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                Log.d("ChatActivity", "Block status check response: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    boolean isBlocked = response.body();
                    Log.d("ChatActivity", "Is blocked: " + isBlocked);
                    updateBlockedUI(isBlocked);
                } else {
                    Log.e("ChatActivity", "Failed to get block status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("ChatActivity", "Failed to check block status", t);
            }
        });
    }

    private void updateBlockedUI(boolean isBlocked) {
        Log.d("ChatActivity", "Updating UI for blocked status: " + isBlocked);
        isBlockedByUser = isBlocked;

        runOnUiThread(() -> {
            if (messageInputLayout != null && blockedMessageLayout != null) {
                messageInputLayout.setVisibility(isBlocked ? View.GONE : View.VISIBLE);
                blockedMessageLayout.setVisibility(isBlocked ? View.VISIBLE : View.GONE);
                Log.d("ChatActivity", "UI updated - Input visibility: " +
                        (isBlocked ? "GONE" : "VISIBLE") +
                        ", Blocked message visibility: " +
                        (isBlocked ? "VISIBLE" : "GONE"));
            } else {
                Log.e("ChatActivity", "Views are null - messageInputLayout: " +
                        (messageInputLayout == null) +
                        ", blockedMessageLayout: " +
                        (blockedMessageLayout == null));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (blockStatusReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(blockStatusReceiver);
        }
        if (webSocket != null) {
            webSocket.close();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webSocket != null) {
            webSocket.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check block status when returning to activity
        checkBlockStatus();
        if (currentUser != null && webSocket != null) {
            webSocket.connectAndSubscribe(currentUser);
        }
    }

    private void markMessagesAsRead() {
        if (currentUser == null || receiverUser == null) return;

        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.markAsRead(currentUser, receiverUser).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Use LocalBroadcastManager to send the broadcast
                    Intent intent = new Intent("MESSAGES_READ");
                    intent.putExtra("contactUsername", receiverUser);
                    LocalBroadcastManager.getInstance(ChatActivity.this)
                            .sendBroadcast(intent);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ChatActivity", "Network error marking messages as read", t);
            }
        });
    }

    private void startVideoCall() {
//        if (receiverUser == null) return;
//
//        Intent intent = new Intent(this, VideoCallActivity.class);
//        intent.putExtra("targetUser", receiverUser);
//        startActivity(intent);
    }
}
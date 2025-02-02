package com.example.projectmxh.screen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

public class ChatActivity extends AppCompatActivity {
    private WebSocketConfig webSocket;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private String currentUser;
    private String receiverUser;

    // UI Elements
    private RecyclerView chatMessages;
    private EditText messageEditText;
    private ImageView sendIcon, backIcon;
    private CircleImageView avatar;
    private TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        initializeViews();
        setupClickListeners();
        setupChat();
    }

    private void initializeViews() {
        chatMessages = findViewById(R.id.chatMessages);
        messageEditText = findViewById(R.id.messageEditText);
        sendIcon = findViewById(R.id.sendIcon);
        backIcon = findViewById(R.id.backIcon);
        avatar = findViewById(R.id.avatar);
        userName = findViewById(R.id.userName);
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
    }

    private void setupChat() {
        receiverUser = getIntent().getStringExtra("receiverName");
        String receiverFullname = getIntent().getStringExtra("receiverFullName");
        String avatarUrl = getIntent().getStringExtra("avatarUrl");

        userName.setText(receiverFullname);
        Glide.with(this).load(avatarUrl).placeholder(R.drawable.ic_avatar).into(avatar);

        messages = new ArrayList<>();

        // Get current user first, then setup adapter and WebSocket
        getCurrentUserAsync(username -> {
            currentUser = username;
            // Initialize adapter after we have currentUser
            chatAdapter = new ChatAdapter(messages, currentUser);
            chatMessages.setLayoutManager(new LinearLayoutManager(this));
            chatMessages.setAdapter(chatAdapter);

            webSocket = new WebSocketConfig(this);
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
        webSocket.send(jsonMessage);
    }

    public void handleNewMessage(String messageJson) {
        Message message = new Gson().fromJson(messageJson, Message.class);
        messages.add(message);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            chatMessages.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close();
        }
    }
}
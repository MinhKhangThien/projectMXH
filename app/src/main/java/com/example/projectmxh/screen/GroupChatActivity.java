package com.example.projectmxh.screen;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectmxh.Model.Message;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupChatActivity extends AppCompatActivity {
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

        groupId = getIntent().getStringExtra("groupId");
        initViews();
        getCurrentUser();
        setupWebSocket();
        loadGroupInfo();
        loadMessages();
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
        adapter = new GroupChatAdapter(messages, currentUser);
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

    private void loadGroupInfo() {
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
                Toast.makeText(GroupChatActivity.this,
                        "Failed to load group info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGroupInfo(List<AppUserDto> members) {
        groupStatus.setText(members.size() + " members");
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            groupChatMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    private void setupWebSocket() {
        webSocket = new WebSocketConfig(this);
        webSocket.subscribeToGroupChat(groupId);
    }

    private void getCurrentUser() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body().getUsername();
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                Log.e(TAG, "Failed to get current user", t);
            }
        });
    }

    public void handleNewMessage(String messageJson) {
        Message message = new Gson().fromJson(messageJson, Message.class);
        runOnUiThread(() -> {
            messages.add(message);
            adapter.notifyItemInserted(messages.size() - 1);
            scrollToBottom();
        });
    }

    public String getGroupId() {
        return groupId;
    }

    private void sendMessage(String content) {
        Message message = new Message();
        message.setSenderName(currentUser);
        message.setMessage(content);
        message.setDate(new Date().toString());

        String destination = "/app/group-message/" + groupId;
        webSocket.send(destination, new Gson().toJson(message));
    }

    private void loadMessages() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getGroupChatMessages(groupId).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messages.clear();
                    messages.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Toast.makeText(GroupChatActivity.this,
                        "Failed to load messages", Toast.LENGTH_SHORT).show();
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

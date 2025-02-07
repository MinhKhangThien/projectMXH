package com.example.projectmxh.screen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.Model.ChatBox;
import com.example.projectmxh.Model.GroupChat;
import com.example.projectmxh.R;
import com.example.projectmxh.adapter.MessageAdapter;
import com.example.projectmxh.Model.User;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.dto.request.PendingFollowRequest;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    private RecyclerView messagesList;
    private MessageAdapter messageAdapter;
    private List<ChatBox> chatBoxes;
    private ImageView messageIcon;

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

        loadBoxChats();
    }

    private void loadBoxChats() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        // First get current user's ID
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String userId = String.valueOf(response.body().getId());
                    loadFollowings(userId);
                    loadGroupChats();
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                Log.e("MessageActivity", "Failed to get current user: " + t.getMessage());
                Toast.makeText(MessageActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupChats() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getGroupChats().enqueue(new Callback<List<GroupChat>>() {
            @Override
            public void onResponse(Call<List<GroupChat>> call, Response<List<GroupChat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (GroupChat group : response.body()) {
                        chatBoxes.add(new ChatBox(
                                group.getId(),
                                group.getName(),
                                null,
                                group.getImage(),
                                true,
                                "Group chat" // You can add last message if available
                        ));
                    }
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<GroupChat>> call, Throwable t) {
                Log.e("MessageActivity", "Failed to get current user: " + t.getMessage());
                Toast.makeText(MessageActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
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
                                "default_avatar_url",
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
}

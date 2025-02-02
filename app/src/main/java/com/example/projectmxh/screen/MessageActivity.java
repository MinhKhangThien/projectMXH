package com.example.projectmxh.screen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private List<User> users; // Danh sách người đã chat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages);

        messagesList = findViewById(R.id.messages_list);
        users = new ArrayList<>();

        messageAdapter = new MessageAdapter(users, user -> {
            // Chuyển sang ChatActivity khi nhấn vào box chat
            Intent intent = new Intent(MessageActivity.this, ChatActivity.class);
            intent.putExtra("receiverName", user.getUserName());
            intent.putExtra("receiverFullName", user.getFullName());
            intent.putExtra("avatarUrl", "https://i.pinimg.com/474x/b7/14/a2/b714a2713d5d9259dab2a7c0b7df4ff9.jpg");
            startActivity(intent);
        });

        messagesList.setLayoutManager(new LinearLayoutManager(this));
        messagesList.setAdapter(messageAdapter);

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
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
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
                    users.clear();
                    for (PendingFollowRequest following : response.body()) {
                        users.add(new User(
                                following.getId(),
                                following.getUserName(),
                                following.getBio(),
                                "",  // gender not provided in response
                                following.getFullName()
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
}

package com.example.projectmxh.screen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmxh.BaseActivity;
import com.example.projectmxh.Model.Message;
import com.example.projectmxh.R;
import com.example.projectmxh.adapter.ChatAdapter;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailChatActivity extends AppCompatActivity {
    private ImageView backIcon;
    private CircleImageView avatarImageView;
    private TextView usernameTextView;
    private String userId, username, fullname, avatarUrl;
    private boolean isBlocked = false;
    private TextView blockUserText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_chat);

        // Get data from intent
        userId = getIntent().getStringExtra("userId");
        Log.d("DetailChatActivity", "User ID: " + userId);
        username = getIntent().getStringExtra("username");
        fullname = getIntent().getStringExtra("fullname");
        avatarUrl = getIntent().getStringExtra("avatarUrl");

        initializeViews();
        setupClickListeners();
        loadUserBlockStatus();
    }

    private void initializeViews() {
        backIcon = findViewById(R.id.backIcon);
        avatarImageView = findViewById(R.id.avatarImageView);
        usernameTextView = findViewById(R.id.usernameTextView);
        blockUserText = findViewById(R.id.blockUserText);

        // Set user data
        usernameTextView.setText(fullname);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.avatar)
                    .into(avatarImageView);
        }
    }

    private void setupClickListeners() {
        backIcon.setOnClickListener(v -> finish());

        findViewById(R.id.blockLayout).setOnClickListener(v -> {
            if (isBlocked) {
                unblockUser();
            } else {
                blockUser();
            }
        });

        findViewById(R.id.profileLayout).setOnClickListener(v -> {
            Intent intent = new Intent(this, BaseActivity.class);
            // Clear top flag to prevent returning to home
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Add extras
            intent.putExtra("loadFragment", "otherProfile");
            intent.putExtra("userId", userId);
            // Add flag to indicate this is from chat
            intent.putExtra("fromChat", true);
            startActivity(intent);
        });

        backIcon.setOnClickListener(v -> finish());

        findViewById(R.id.blockLayout).setOnClickListener(v -> {
            if (isBlocked) {
                showUnblockConfirmDialog();
            } else {
                showBlockConfirmDialog();
            }
        });
    }

    private void loadUserBlockStatus() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.isBlockedUser(userId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isBlocked = response.body();
                    updateBlockUI();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(DetailChatActivity.this,
                        "Failed to check block status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void blockUser() {
        Log.d("DetailChatActivity", "Blocking user: " + userId);
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.blockUser(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isBlocked = true;
                    updateBlockUI();
                    // Send broadcast to update ChatActivity
                    Intent intent = new Intent("USER_BLOCKED");
                    intent.putExtra("blockedUserId", userId);
                    LocalBroadcastManager.getInstance(DetailChatActivity.this)
                            .sendBroadcast(intent);
                    Toast.makeText(DetailChatActivity.this,
                            "User blocked successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("DetailChatActivity", "Block failed: " + response.code());
                    Toast.makeText(DetailChatActivity.this,
                            "Failed to block user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("DetailChatActivity", "Block request failed", t);
                Toast.makeText(DetailChatActivity.this,
                        "Failed to block user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unblockUser() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.unblockUser(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isBlocked = false;
                    updateBlockUI();
                    Toast.makeText(DetailChatActivity.this,
                            "User unblocked successfully", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DetailChatActivity.this,
                        "Failed to unblock user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBlockUI() {
        blockUserText.setText(isBlocked ? "Unblock User" : "Block User");
        blockUserText.setTextColor(getColor(isBlocked ? R.color.blue : R.color.red));
    }

    private void showBlockConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Block User")
                .setMessage("Are you sure you want to block this user? They won't be able to send you messages.")
                .setPositiveButton("Block", (dialog, which) -> {
                    blockUser();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showUnblockConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Unblock User")
                .setMessage("Are you sure you want to unblock this user?")
                .setPositiveButton("Unblock", (dialog, which) -> {
                    unblockUser();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
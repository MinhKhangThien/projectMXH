package com.example.projectmxh.screen;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectmxh.R;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupChatDetailActivity extends AppCompatActivity {
    private ImageView backIcon;
    private ImageView avatarImageView;
    private TextView usernameTextView;
    private String groupId;
    private List<AppUserDto> members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_groupchat);

        groupId = getIntent().getStringExtra("groupId");
        initViews();
        loadGroupInfo();
    }

    private void initViews() {
        backIcon = findViewById(R.id.backIcon);
        avatarImageView = findViewById(R.id.avatarImageView);
        usernameTextView = findViewById(R.id.usernameTextView);

        backIcon.setOnClickListener(v -> finish());

        // Add member click listener
        findViewById(R.id.optionalIcon).setOnClickListener(v -> {
            // TODO: Show add member dialog
        });

        // Rename group click listener
        findViewById(R.id.renameGroup).setOnClickListener(v -> showRenameDialog());

        // Leave group click listener
        findViewById(R.id.leaveGroup).setOnClickListener(v -> handleLeaveGroup());
    }

    private void loadGroupInfo() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getGroupChatUsers(groupId).enqueue(new Callback<List<AppUserDto>>() {
            @Override
            public void onResponse(Call<List<AppUserDto>> call, Response<List<AppUserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    members = response.body();
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<List<AppUserDto>> call, Throwable t) {
                Toast.makeText(GroupChatDetailActivity.this,
                        "Failed to load group info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRenameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Group");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newName = input.getText().toString();
            if (!newName.isEmpty()) {
                renameGroup(newName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void renameGroup(String newName) {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        // TODO: Implement rename API call
    }

    private void handleLeaveGroup() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave this group?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    leaveGroup();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void leaveGroup() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.removeUserFromGroupChat(groupId, getCurrentUserId())
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            finish();
                        } else {
                            Toast.makeText(GroupChatDetailActivity.this,
                                    "Failed to leave group", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(GroupChatDetailActivity.this,
                                "Error leaving group", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getCurrentUserId() {
        // TODO: Get current user ID from shared preferences or other storage
        return "";
    }

    private void updateUI() {
        if (members != null && !members.isEmpty()) {
            usernameTextView.setText(String.format("%d members", members.size()));
        }
    }
}

package com.example.projectmxh.screen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmxh.R;
import com.example.projectmxh.adapter.GroupMemberAdapter;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupChatDetailActivity extends AppCompatActivity {
    private static final int ADD_MEMBER_REQUEST = 1001;
    private ImageView backIcon;
    private ImageView avatarImageView;
    private TextView usernameTextView;
    private List<AppUserDto> members;

    private String groupId;
    private String groupName;
    private String groupImage;

    private RecyclerView membersList;
    private GroupMemberAdapter memberAdapter;

    private TextView groupNameText;
    private TextView memberCount;
    private CircleImageView groupAvatar;
    private String hostId;

    private static final String TAG = "GroupChatDetailActivity";

    // Views
    private View optionalIcon;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_groupchat);

        // Get intent data
        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");
        groupImage = getIntent().getStringExtra("groupImage");

        if (groupId == null) {
            Log.e(TAG, "No group ID provided");
            Toast.makeText(this, "Error: Invalid group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupInitialData();
        setupClickListeners();
        getCurrentUserAndLoadData();
    }

    private void setupMembersList() {
        memberAdapter = new GroupMemberAdapter(new ArrayList<>(), currentUserId, groupId);
        membersList.setAdapter(memberAdapter);
        loadGroupMembers();
    }

    private void loadGroupMembers() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getGroupChatUsers(groupId).enqueue(new Callback<List<AppUserDto>>() {
            @Override
            public void onResponse(Call<List<AppUserDto>> call, Response<List<AppUserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AppUserDto> members = response.body();
                    memberAdapter.updateMembers(members);
                    updateMemberCount(members.size());
                }
            }

            @Override
            public void onFailure(Call<List<AppUserDto>> call, Throwable t) {
                Log.e(TAG, "Failed to load members", t);
            }
        });
    }

    private void updateMemberCount(int count) {
        if (memberCount != null) {
            memberCount.setText(count + (count == 1 ? " member" : " members"));
        }
    }

    private void initViews() {
        backIcon = findViewById(R.id.backIcon);
        avatarImageView = findViewById(R.id.avatarImageView);
        usernameTextView = findViewById(R.id.usernameTextView);

        groupNameText = findViewById(R.id.groupNameText);
        memberCount = findViewById(R.id.memberCount);
        groupAvatar = findViewById(R.id.groupAvatar);
        membersList = findViewById(R.id.membersList);
        optionalIcon = findViewById(R.id.optionalIcon);

        membersList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupInitialData() {
        if (groupName != null) {
            groupNameText.setText(groupName);
        }

        if (groupImage != null && !groupImage.isEmpty()) {
            Glide.with(this)
                    .load(groupImage)
                    .placeholder(R.drawable.ic_groupchat)
                    .into(groupAvatar);
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.backIcon).setOnClickListener(v -> finish());
        findViewById(R.id.optionalIcon).setOnClickListener(v -> showAddMemberDialog());
        findViewById(R.id.renameGroup).setOnClickListener(v -> showRenameDialog());
        findViewById(R.id.leaveGroup).setOnClickListener(v -> showLeaveGroupDialog());
    }

    private void getCurrentUserAndLoadData() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUserId = response.body().getId();
                    loadGroupHost();
                    setupMembersList();
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                Log.e(TAG, "Failed to get current user", t);
            }
        });
    }

    private void loadGroupHost() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getGroupHost(groupId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    hostId = response.body();
                    updateHostUI();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Failed to get host info", t);
            }
        });
    }

    private void updateHostUI() {
        boolean isHost = currentUserId != null && currentUserId.equals(hostId);
//        optionalIcon.setVisibility(isHost ? View.VISIBLE : View.GONE);
    }

    private void loadGroupInfo() {
        Log.d("GroupChatDetail", "Loading info for group: " + groupId);
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);

        // Get current user first
        apiService.getGroupHost(groupId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    hostId = response.body();

                    // Get current user to check permissions
                    apiService.getMe().enqueue(new Callback<AppUserDto>() {
                        @Override
                        public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String currentUserId = response.body().getId();
                                boolean isHost = currentUserId.equals(hostId);

                                // Show/hide host-only features
                                findViewById(R.id.optionalIcon).setVisibility(
                                        isHost ? View.VISIBLE : View.GONE
                                );
                            }
                        }

                        @Override
                        public void onFailure(Call<AppUserDto> call, Throwable t) {
                            Log.e("GroupChatDetail", "Failed to get current user", t);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("GroupChatDetail", "Failed to get host info", t);
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
        apiService.removeUserFromGroupChat(groupId, currentUserId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(GroupChatDetailActivity.this,
                                    "Left group successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(GroupChatDetailActivity.this,
                                    "Failed to leave group", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Network error leaving group", t);
                        Toast.makeText(GroupChatDetailActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupAddMemberButton() {
        findViewById(R.id.optionalIcon).setOnClickListener(v -> {
            Intent intent = new Intent(this, UserSelectionActivity.class);
            intent.putExtra("groupId", groupId);
            startActivityForResult(intent, ADD_MEMBER_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_MEMBER_REQUEST && resultCode == RESULT_OK) {
            loadGroupMembers();
        }
    }

    private void showAddMemberDialog() {
        Intent intent = new Intent(this, CreateGroupChatActivity.class);
        intent.putExtra("isAddingMembers", true);
        intent.putExtra("groupId", groupId);
        startActivityForResult(intent, ADD_MEMBER_REQUEST);
    }

    private void showLeaveGroupDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave this group?")
                .setPositiveButton("Leave", (dialog, which) -> leaveGroup())
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void updateUI(List<AppUserDto> members) {
        if (memberCount != null) {
            memberCount.setText(String.format("%d members", members.size()));
        }
    }
}

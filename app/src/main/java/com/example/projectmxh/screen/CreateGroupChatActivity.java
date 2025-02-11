package com.example.projectmxh.screen;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.R;
import com.example.projectmxh.adapter.UserSelectionAdapter;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.dto.GroupChatDto;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateGroupChatActivity extends AppCompatActivity {
    private static final String TAG = "CreateGroupChatActivity";

    private RecyclerView userList;
    private EditText searchInput;
    private Button createGroupButton;
    private UserSelectionAdapter adapter;
    private List<AppUserDto> selectedUsers;
    private TextView titleTextView;

    private boolean isAddingMembers;
    private String existingGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        // Initialize selectedUsers list
        selectedUsers = new ArrayList<>();

        initViews();
        setupIntentData();
        setupSearchInput();
    }

    private void initViews() {
        // Initialize views
        titleTextView = findViewById(R.id.titleTextView);
        createGroupButton = findViewById(R.id.createGroupButton);
        userList = findViewById(R.id.userList);
        searchInput = findViewById(R.id.searchInput);

        // Setup RecyclerView
        userList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserSelectionAdapter(new ArrayList<>(), user -> {
            if (selectedUsers.contains(user)) {
                Log.d(TAG, "Removing user: " + user.getDisplayName());
                selectedUsers.remove(user);
            } else {
                Log.d(TAG, "Adding user: " + user.getDisplayName());
                selectedUsers.add(user);
            }
            updateCreateButtonState();
        });
        userList.setAdapter(adapter);

        // Setup click listeners
        findViewById(R.id.backIcon).setOnClickListener(v -> finish());

        if (createGroupButton != null) {
            createGroupButton.setOnClickListener(v -> handleCreateButtonClick());
            createGroupButton.setEnabled(false);
        }
    }

    private void handleCreateButtonClick() {
        if (!selectedUsers.isEmpty()) {
            List<String> userIds = selectedUsers.stream()
                    .map(user -> user.getId().toString())
                    .collect(Collectors.toList());
            if (isAddingMembers) {
                addMembersToGroup(userIds);
            } else {
                createGroupChat(userIds);
            }
        }
    }

    private void setupIntentData() {
        isAddingMembers = getIntent().getBooleanExtra("isAddingMembers", false);
        existingGroupId = getIntent().getStringExtra("groupId");

        if (titleTextView != null) {
            titleTextView.setText(isAddingMembers ? "Add Members" : "Create Group");
        }
        if (createGroupButton != null) {
            createGroupButton.setText(isAddingMembers ? "Add Members" : "Create");
        }
    }

    private void setupSearchInput() {
        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    String query = s.toString().trim();
                    if (!query.isEmpty()) {
                        searchUsers(query);
                    }
                }
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });
        }
    }

    private void searchUsers(String query) {
        Log.d(TAG, "Searching users with query: " + query);
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.searchUsers(query).enqueue(new Callback<List<AppUserDto>>() {
            @Override
            public void onResponse(Call<List<AppUserDto>> call, Response<List<AppUserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Found " + response.body().size() + " users");
                    adapter.updateUsers(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<AppUserDto>> call, Throwable t) {
                Log.e(TAG, "Error searching users", t);
                Toast.makeText(CreateGroupChatActivity.this,
                        "Failed to search users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCreateButtonState() {
        runOnUiThread(() -> {
            if (createGroupButton != null) {
                boolean enabled = selectedUsers != null && !selectedUsers.isEmpty();
                Log.d(TAG, "Updating button state. Selected users: " +
                        (selectedUsers != null ? selectedUsers.size() : 0));
                createGroupButton.setEnabled(enabled);
                createGroupButton.setAlpha(enabled ? 1.0f : 0.5f);
            }
        });
    }

    private void createGroupChat(List<String> userIds) {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);

        // Get current user first
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String currentUserId = response.body().getId();

                    // Add current user to the list if not already present
                    if (!userIds.contains(currentUserId)) {
                        userIds.add(currentUserId);
                    }

                    Log.d(TAG, "Creating group chat with users: " + userIds);
                    String defaultGroupName = "New Group Chat";
                    String defaultGroupImage = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSgsPvKePubVhGBwlIPQeL9fDFLGjWxZ2wSNA&s";

                    apiService.createGroupChat(userIds, defaultGroupName, defaultGroupImage)
                            .enqueue(new Callback<GroupChatDto>() {
                                @Override
                                public void onResponse(Call<GroupChatDto> call, Response<GroupChatDto> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        GroupChatDto group = response.body();
                                        Log.d(TAG, "Group created successfully: " + group.getId());

                                        Intent intent = new Intent(CreateGroupChatActivity.this, GroupChatActivity.class);
                                        intent.putExtra("groupId", group.getId());
                                        intent.putExtra("groupName", group.getName());
                                        intent.putExtra("groupImage", group.getImage());
                                        intent.putExtra("isHost", true);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Log.e(TAG, "Failed to create group: " + response.code());
                                        Toast.makeText(CreateGroupChatActivity.this,
                                                "Failed to create group chat", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<GroupChatDto> call, Throwable t) {
                                    Log.e(TAG, "Network error creating group", t);
                                    Toast.makeText(CreateGroupChatActivity.this,
                                            "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Log.e(TAG, "Failed to get current user");
                    Toast.makeText(CreateGroupChatActivity.this,
                            "Failed to get user info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                Log.e(TAG, "Network error getting current user", t);
                Toast.makeText(CreateGroupChatActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMembersToGroup(List<String> userIds) {
        if (existingGroupId == null) return;

        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        final int totalUsers = userIds.size();
        final int[] successCount = {0};
        final int[] failCount = {0};

        for (String userId : userIds) {
            Log.d(TAG, "Adding user: " + userId + " to group: " + existingGroupId);

            apiService.addUserToGroupChat(existingGroupId, userId)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                successCount[0]++;
                                Log.d(TAG, "Successfully added user. Success count: " + successCount[0]);
                            } else {
                                failCount[0]++;
                                Log.e(TAG, "Failed to add user. Error: " + response.code());
                            }

                            // Check if all operations are complete
                            if (successCount[0] + failCount[0] == totalUsers) {
                                runOnUiThread(() -> {
                                    if (successCount[0] > 0) {
                                        Toast.makeText(CreateGroupChatActivity.this,
                                                "Successfully added " + successCount[0] + " members",
                                                Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                    if (failCount[0] > 0) {
                                        Toast.makeText(CreateGroupChatActivity.this,
                                                "Failed to add " + failCount[0] + " members",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            failCount[0]++;
                            Log.e(TAG, "Network error adding user", t);

                            if (successCount[0] + failCount[0] == totalUsers) {
                                runOnUiThread(() -> {
                                    Toast.makeText(CreateGroupChatActivity.this,
                                            "Network error adding members",
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });
        }
    }

}

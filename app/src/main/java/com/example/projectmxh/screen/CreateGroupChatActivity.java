package com.example.projectmxh.screen;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
    private RecyclerView userList;
    private EditText searchInput;
    private Button createGroupButton;
    private UserSelectionAdapter adapter;
    private List<AppUserDto> selectedUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        initViews();
        setupSearchInput();
    }

    private void initViews() {
        userList = findViewById(R.id.userList);
        searchInput = findViewById(R.id.searchInput);
        createGroupButton = findViewById(R.id.createGroupButton);
        createGroupButton.setEnabled(false); // Ensure initially disabled

        adapter = new UserSelectionAdapter(new ArrayList<>(), user -> {
            if (selectedUsers.contains(user)) {
                selectedUsers.remove(user);
            } else {
                selectedUsers.add(user);
            }
            updateCreateButtonState();
            Log.d("CreateGroupChat", "Selected users: " + selectedUsers.size()); // Debug log
        });

        userList.setLayoutManager(new LinearLayoutManager(this));
        userList.setAdapter(adapter);

        findViewById(R.id.backIcon).setOnClickListener(v -> finish());
        createGroupButton.setOnClickListener(v -> createGroup());
    }

    private void setupSearchInput() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                searchUsers(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void searchUsers(String query) {
        if (query.isEmpty()) return;

        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.searchUsers(query).enqueue(new Callback<List<AppUserDto>>() {
            @Override
            public void onResponse(Call<List<AppUserDto>> call, Response<List<AppUserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateUsers(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<AppUserDto>> call, Throwable t) {
                Toast.makeText(CreateGroupChatActivity.this,
                        "Failed to search users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createGroup() {
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "Please select users", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> userIds = selectedUsers.stream()
                .map(user -> user.getId().toString())
                .collect(Collectors.toList());

        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.createGroupChat(userIds).enqueue(new Callback<GroupChatDto>() {
            @Override
            public void onResponse(Call<GroupChatDto> call, Response<GroupChatDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(CreateGroupChatActivity.this, GroupChatActivity.class);
                    intent.putExtra("groupId", response.body().getId().toString());
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<GroupChatDto> call, Throwable t) {
                Toast.makeText(CreateGroupChatActivity.this,
                        "Failed to create group", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCreateButtonState() {
        runOnUiThread(() -> {
            boolean enabled = !selectedUsers.isEmpty();
            createGroupButton.setEnabled(enabled);
            Log.d("CreateGroupChat", "Button enabled: " + enabled); // Debug log
        });
    }
}

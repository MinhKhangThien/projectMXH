package com.example.projectmxh.screen;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.R;
import com.example.projectmxh.adapter.UserSelectionAdapter;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserSelectionActivity extends AppCompatActivity {
    private RecyclerView userList;
    private EditText searchInput;
    private UserSelectionAdapter adapter;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_selection);

        groupId = getIntent().getStringExtra("groupId");
        initViews();
        setupSearchInput();
    }

    private void initViews() {
        userList = findViewById(R.id.userList);
        searchInput = findViewById(R.id.searchInput);

        adapter = new UserSelectionAdapter(new ArrayList<>(), user -> {
            addUserToGroup(user.getId().toString());
        });

        userList.setLayoutManager(new LinearLayoutManager(this));
        userList.setAdapter(adapter);

        findViewById(R.id.backIcon).setOnClickListener(v -> finish());
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
                Toast.makeText(UserSelectionActivity.this,
                        "Failed to search users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addUserToGroup(String userId) {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.addUserToGroupChat(groupId, userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(UserSelectionActivity.this,
                            "Failed to add user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(UserSelectionActivity.this,
                        "Error adding user", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
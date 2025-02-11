package com.example.projectmxh.screen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.R;
import com.example.projectmxh.adapter.UserManagementAdapter;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.interfaces.UserManagementRefreshListener;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActiveUsersFragment extends Fragment {
    private RecyclerView userList;
    private UserManagementAdapter adapter;
    private EditText searchInput;
    private Button searchButton;
    private Button banButton;
    private List<AppUserDto> selectedUsers = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Retain instance across configuration changes
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_user_management, container, false);
        initViews(view);
        loadActiveUsers();
        return view;
    }

    private void initViews(View view) {
        userList = view.findViewById(R.id.user_list);
        searchInput = view.findViewById(R.id.search_input);
        searchButton = view.findViewById(R.id.search_button);
        banButton = view.findViewById(R.id.delete_user_button);
        banButton.setText("Ban Selected Users");

        // Setup RecyclerView and adapter only once
        userList.setLayoutManager(new LinearLayoutManager(getContext()));
        if (adapter == null) {
            adapter = new UserManagementAdapter(new ArrayList<>(), user -> {
                if (selectedUsers.contains(user)) {
                    selectedUsers.remove(user);
                } else {
                    selectedUsers.add(user);
                }
                updateBanButtonState();
            });
            userList.setAdapter(adapter);
        }

        searchButton.setOnClickListener(v -> performSearch());
        banButton.setOnClickListener(v -> banSelectedUsers());
    }

    public void loadActiveUsers() {
        ApiService apiService = ApiClient.getClientWithToken(getContext()).create(ApiService.class);
        apiService.getActiveUsers().enqueue(new Callback<List<AppUserDto>>() {
            @Override
            public void onResponse(Call<List<AppUserDto>> call, Response<List<AppUserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateUsers(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<AppUserDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load active users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void banSelectedUsers() {
        if (selectedUsers.isEmpty()) {
            return;
        }

        List<String> userIds = selectedUsers.stream()
                .map(AppUserDto::getId)
                .collect(Collectors.toList());

        ApiService apiService = ApiClient.getClientWithToken(getContext()).create(ApiService.class);
        apiService.batchUpdateUserBanStatus(true, userIds).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(),
                            selectedUsers.size() + " users banned successfully",
                            Toast.LENGTH_SHORT).show();
                    selectedUsers.clear();
                    adapter.clearSelection();
                    updateBanButtonState();
                    loadActiveUsers();

                    // Notify container to refresh other fragment
                    if (getParentFragment() instanceof UserManagementRefreshListener) {
                        ((UserManagementRefreshListener) getParentFragment()).onUserStatusChanged();
                    }
                } else {
                    Toast.makeText(getContext(),
                            "Failed to ban users: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Network error while banning users",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBanButtonState() {
        banButton.setEnabled(!selectedUsers.isEmpty());
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            loadActiveUsers();
            return;
        }

        ApiService apiService = ApiClient.getClientWithToken(getContext()).create(ApiService.class);
        apiService.searchUsers(query).enqueue(new Callback<List<AppUserDto>>() {
            @Override
            public void onResponse(Call<List<AppUserDto>> call, Response<List<AppUserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Filter only active users from search results
                    List<AppUserDto> activeUsers = response.body().stream()
                            .filter(user -> {
                                Boolean isBanned = user.getIsBanned();
                                return isBanned == null || !isBanned; // Consider null as not banned
                            })
                            .collect(Collectors.toList());
                    adapter.updateUsers(activeUsers);
                }
            }

            @Override
            public void onFailure(Call<List<AppUserDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to search users", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

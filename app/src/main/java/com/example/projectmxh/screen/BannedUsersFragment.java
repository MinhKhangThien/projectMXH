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

public class BannedUsersFragment extends Fragment {
    private RecyclerView userList;
    private UserManagementAdapter adapter;
    private EditText searchInput;
    private Button searchButton;
    private Button unbanButton;
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
        loadBannedUsers();
        return view;
    }

    private void initViews(View view) {
        userList = view.findViewById(R.id.user_list);
        searchInput = view.findViewById(R.id.search_input);
        searchButton = view.findViewById(R.id.search_button);
        unbanButton = view.findViewById(R.id.delete_user_button);
        unbanButton.setText("Unban Selected Users");

        // Setup RecyclerView and adapter only once
        userList.setLayoutManager(new LinearLayoutManager(getContext()));
        if (adapter == null) {
            adapter = new UserManagementAdapter(new ArrayList<>(), user -> {
                if (selectedUsers.contains(user)) {
                    selectedUsers.remove(user);
                } else {
                    selectedUsers.add(user);
                }
                updateUnbanButtonState();
            });
            userList.setAdapter(adapter);
        }

        searchButton.setOnClickListener(v -> performSearch());
        unbanButton.setOnClickListener(v -> unbanSelectedUsers());
    }

    public void loadBannedUsers() {
        ApiService apiService = ApiClient.getClientWithToken(getContext()).create(ApiService.class);
        apiService.getBannedUsers().enqueue(new Callback<List<AppUserDto>>() {
            @Override
            public void onResponse(Call<List<AppUserDto>> call, Response<List<AppUserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateUsers(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<AppUserDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load banned users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unbanSelectedUsers() {
        if (selectedUsers.isEmpty()) {
            return;
        }

        List<String> userIds = selectedUsers.stream()
                .map(AppUserDto::getId)
                .collect(Collectors.toList());

        ApiService apiService = ApiClient.getClientWithToken(getContext()).create(ApiService.class);
        apiService.batchUpdateUserBanStatus(false, userIds).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(),
                            selectedUsers.size() + " users unbanned successfully",
                            Toast.LENGTH_SHORT).show();
                    selectedUsers.clear();
                    adapter.clearSelection();
                    updateUnbanButtonState();
                    loadBannedUsers();

                    if (getParentFragment() instanceof UserManagementRefreshListener) {
                        ((UserManagementRefreshListener) getParentFragment()).onUserStatusChanged();
                    }
                } else {
                    Toast.makeText(getContext(),
                            "Failed to unban users: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Network error while unbanning users",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUnbanButtonState() {
        unbanButton.setEnabled(!selectedUsers.isEmpty());
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            loadBannedUsers();
            return;
        }

        ApiService apiService = ApiClient.getClientWithToken(getContext()).create(ApiService.class);
        apiService.searchUsers(query).enqueue(new Callback<List<AppUserDto>>() {
            @Override
            public void onResponse(Call<List<AppUserDto>> call, Response<List<AppUserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Filter only banned users from search results
                    List<AppUserDto> bannedUsers = response.body().stream()
                            .filter(user -> {
                                Boolean isBanned = user.getIsBanned();
                                return isBanned != null && isBanned; // Only true values
                            })
                            .collect(Collectors.toList());
                    adapter.updateUsers(bannedUsers);
                }
            }

            @Override
            public void onFailure(Call<List<AppUserDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to search users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

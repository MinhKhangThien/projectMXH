package com.example.projectmxh.screen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.R;
import com.example.projectmxh.adapter.NotificationAdapter;
import com.example.projectmxh.dto.NotificationUserResponseDto;
import com.example.projectmxh.dto.PageData;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment implements NotificationAdapter.NotificationClickListener {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationUserResponseDto> notifications;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.notification, container, false);
        initViews();
        loadNotifications();
        setupClickListeners();
        return rootView;
    }

    private void initViews() {
        recyclerView = rootView.findViewById(R.id.recyclerView);
        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(notifications, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        View requestRow = rootView.findViewById(R.id.requestRow);
        requestRow.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AcceptFollowActivity.class));
        });
    }

    private void setupClickListeners() {
        rootView.findViewById(R.id.messageIcon).setOnClickListener(v -> {
            // TODO: Navigate to messages
        });
    }

    private void loadNotifications() {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getNotifications(0, 20, null).enqueue(new Callback<PageData<NotificationUserResponseDto>>() {
            @Override
            public void onResponse(Call<PageData<NotificationUserResponseDto>> call,
                                   Response<PageData<NotificationUserResponseDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    notifications.clear();
                    notifications.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                    Log.d("NotificationsFragment", "Loaded " + notifications.size() + " notifications");
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e("NotificationsFragment", "Error loading notifications. Code: "
                            + response.code() + " Error: " + errorBody);
                    Toast.makeText(getContext(), "Error loading notifications", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageData<NotificationUserResponseDto>> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("NotificationsFragment", "Failed to load notifications", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNotificationClick(NotificationUserResponseDto notification) {
        if (!isAdded()) return;

        switch (notification.getEntityType()) {
            case "follow":
                navigateToUserProfile(notification.getCreatedBy().getId());
                break;
            case "like":
            case "comment":
                navigateToPost(notification.getEntityId());
                break;
            default:
                Log.d("NotificationsFragment", "Unknown notification type: " + notification.getEntityType());
                break;
        }
    }

    private void navigateToPost(String postId) {
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid post", Toast.LENGTH_SHORT).show();
            return;
        }

        PostFragment fragment = PostFragment.newInstance(postId);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToUserProfile(String userId) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid user", Toast.LENGTH_SHORT).show();
            return;
        }

        OtherProfileFragment fragment = new OtherProfileFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }
}
package com.example.projectmxh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.adapter.NotificationAdapter;
import com.example.projectmxh.dto.NotificationUserResponseDto;
import com.example.projectmxh.dto.PageData;
import com.example.projectmxh.screen.ProfileActivity;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity implements NotificationAdapter.NotificationClickListener {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationUserResponseDto> notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        initViews();
        loadNotifications();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(notifications, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getNotifications(0, 20, null).enqueue(new Callback<PageData<NotificationUserResponseDto>>() {
            @Override
            public void onResponse(Call<PageData<NotificationUserResponseDto>> call,
                                   Response<PageData<NotificationUserResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notifications.clear();
                    notifications.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                    Log.d("NotificationsActivity", "Loaded " + notifications.size() + " notifications");
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e("NotificationsActivity", "Error loading notifications. Code: "
                            + response.code() + " Error: " + errorBody);
                    Toast.makeText(NotificationsActivity.this,
                            "Error loading notifications", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageData<NotificationUserResponseDto>> call, Throwable t) {
                Log.e("NotificationsActivity", "Failed to load notifications", t);
                Toast.makeText(NotificationsActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNotificationClick(NotificationUserResponseDto notification) {
        Intent intent;
        if ("follow".equals(notification.getEntityType())) {
            intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("userId", notification.getEntityId());
        } else {
            intent = new Intent(this, BaseActivity.class);
            intent.putExtra("postId", notification.getEntityId());
        }
        startActivity(intent);
    }
}

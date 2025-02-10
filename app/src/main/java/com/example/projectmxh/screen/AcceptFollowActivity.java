package com.example.projectmxh.screen;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.R;
import com.example.projectmxh.adapter.AcceptFollowAdapter;
import com.example.projectmxh.dto.request.PendingFollowRequest;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcceptFollowActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AcceptFollowAdapter adapter;
    private List<PendingFollowRequest> requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accept_follow);

        initViews();
        loadPendingRequests();
    }

    private void initViews() {
        ImageView backIcon = findViewById(R.id.backIcon);
        if (backIcon != null) {
            backIcon.setOnClickListener(v -> finish());
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        requests = new ArrayList<>();
        adapter = new AcceptFollowAdapter(requests,
                new AcceptFollowAdapter.RequestActionListener() {
                    @Override
                    public void onAccept(PendingFollowRequest request) {
                        acceptRequest(request);
                    }

                    @Override
                    public void onDelete(PendingFollowRequest request) {
                        deleteRequest(request);
                    }
                });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.backIcon).setOnClickListener(v -> finish());
    }

    private void loadPendingRequests() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getPendingFollowRequests().enqueue(new Callback<List<PendingFollowRequest>>() {
            @Override
            public void onResponse(Call<List<PendingFollowRequest>> call,
                                   Response<List<PendingFollowRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    requests.clear();
                    requests.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<PendingFollowRequest>> call, Throwable t) {
                Toast.makeText(AcceptFollowActivity.this,
                        "Error loading requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptRequest(PendingFollowRequest request) {
        String requestId = request.getRequestId(); // Use requestId instead of id
        Log.d("AcceptFollow", "Accepting request ID: " + requestId);

        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.acceptPendingRequest(requestId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e("AcceptFollow", "Error body: " + errorBody);
                            } catch (IOException e) {
                                Log.e("AcceptFollow", "Could not read error body", e);
                            }
                        }

                        if (response.isSuccessful()) {
                            Log.d("AcceptFollow", "Request accepted successfully");
                            adapter.removeItem(request);
                            updateEmptyState();
                            Toast.makeText(AcceptFollowActivity.this,
                                    "Request accepted", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("AcceptFollow", "Failed to accept request: " + response.message());
                            adapter.resetButtonState(request);
                            Toast.makeText(AcceptFollowActivity.this,
                                    "Failed to accept request: " + response.message(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("AcceptFollow", "Network error while accepting request", t);
                        adapter.resetButtonState(request);
                        Toast.makeText(AcceptFollowActivity.this,
                                "Error accepting request: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteRequest(PendingFollowRequest request) {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.deletePendingRequest(request.getId().toString())
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            requests.remove(request);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(AcceptFollowActivity.this,
                                    "Request deleted", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(AcceptFollowActivity.this,
                                "Error deleting request", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateEmptyState() {
        if (requests.isEmpty()) {
            // Show empty state message
            TextView emptyView = findViewById(R.id.emptyView);
            if (emptyView != null) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }
    }
}

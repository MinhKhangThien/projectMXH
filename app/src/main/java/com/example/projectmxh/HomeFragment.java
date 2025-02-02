package com.example.projectmxh;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.CommentActivity;
import com.example.projectmxh.CreatePostActivity;
import com.example.projectmxh.R;
import com.example.projectmxh.adapter.PostAdapter;
import com.example.projectmxh.Model.Post;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;
import com.google.android.material.button.MaterialButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment implements PostAdapter.PostClickListener {
    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> posts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActivityResultLauncher<Intent> createPostLauncher;
    private View loadingView;
    private View errorView;
    private MaterialButton retryButton;

    private static final int CREATE_POST_REQUEST = 100;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createPostLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadTimeline();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupListeners();
        loadTimeline();

        return view;
    }

    private void initializeViews(View view) {
        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> loadTimeline());
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright);

        // Initialize RecyclerView
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), posts, this);

        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postsRecyclerView.setAdapter(postAdapter);

        // Setup Quick Post Section
        View quickPostSection = view.findViewById(R.id.quickPostSection);
        quickPostSection.setOnClickListener(v -> navigateToCreatePost());

        // Initialize state views
        loadingView = view.findViewById(R.id.loadingView);
        errorView = view.findViewById(R.id.errorView);
        retryButton = view.findViewById(R.id.retryButton);

        // Setup Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("");
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadTimeline);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright);

        retryButton.setOnClickListener(v -> loadTimeline());
    }

    private void loadTimeline() {
        showLoading();

        ApiService apiService = ApiClient.getClientWithToken(getContext()).create(ApiService.class);
        apiService.getTimeline().enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    showContent();
                    posts.clear();
                    posts.addAll(response.body());

                    // Update like counts for each post
                    for (int i = 0; i < posts.size(); i++) {
                        final int position = i;
                        updateLikeCount(posts.get(i), position);
                    }
                    checkLikeStatusForPosts();
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                showError();
            }
        });
    }

    private void navigateToCreatePost() {
        Intent intent = new Intent(getContext(), CreatePostActivity.class);
        createPostLauncher.launch(intent);
    }

    private void checkLikeStatusForPosts() {
        ApiService apiService = ApiClient.getClientWithToken(getContext()).create(ApiService.class);
        for (Post post : posts) {
            apiService.checkPostLike(post.getId()).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        post.setLiked(response.body());
                        postAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Log.e("HomeFragment", "Failed to check like status: " + t.getMessage());
                }
            });
        }
    }

    @Override
    public void onLikeClick(Post post, int position) {
        ApiService apiService = ApiClient.getClientWithToken(getContext()).create(ApiService.class);
        apiService.likePost(post.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    post.setLiked(!post.isLiked());
                    updateLikeCount(post, position);
                } else {
                    Toast.makeText(getContext(), "Failed to like post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLikeCount(Post post, int position) {
        ApiService apiService = ApiClient.getClientWithToken(getContext()).create(ApiService.class);
        apiService.getLikeCount(post.getId()).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    post.setLikeCount(response.body());
                    postAdapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                // Log error but don't show toast to avoid multiple toasts
                Log.e("HomeFragment", "Error updating like count: " + t.getMessage());
            }
        });
    }

    @Override
    public void onCommentClick(Post post, int position) {
        Intent intent = new Intent(getContext(), CommentActivity.class);
        intent.putExtra("postId", post.getId());
        startActivity(intent);
    }

    @Override
    public void onMoreClick(Post post, int position) {
        View anchor = postsRecyclerView.findViewHolderForAdapterPosition(position)
                .itemView.findViewById(R.id.moreButton);

        PopupMenu popup = new PopupMenu(getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_post_options, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                // Start edit activity
                Intent intent = new Intent(getActivity(), CreatePostActivity.class);
                //intent.putExtra("post", post);
                //startActivityForResult(intent, EDIT_POST_REQUEST);
                return true;
            } else if (itemId == R.id.action_delete) {
                // Show delete confirmation
                //deletePost(post, position);
                return true;
            } else if (itemId == R.id.action_save) {
                // Save post
                //savePost(post);
                return true;
            } else if (itemId == R.id.action_report) {
                // Show report dialog
                //reportPost(post);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        postsRecyclerView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingView.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showContent() {
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        postsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showError() {
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        postsRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CreatePostActivity.REQUEST_CREATE_POST
                && resultCode == Activity.RESULT_OK) {
            loadTimeline();
        }
    }
}


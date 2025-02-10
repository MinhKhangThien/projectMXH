package com.example.projectmxh.screen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.projectmxh.CommentActivity;
import com.example.projectmxh.Model.Post;
import com.example.projectmxh.R;
import com.example.projectmxh.adapter.PostAdapter;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPostsFragment extends Fragment implements PostAdapter.PostClickListener {
    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> posts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View loadingView, errorView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_posts, container, false);
        initViews(view);
        loadUserPosts();
        return view;
    }

    private void initViews(View view) {
        if (view != null) {
            postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            loadingView = view.findViewById(R.id.loadingView);
            errorView = view.findViewById(R.id.errorView);

            postsRecyclerView.setClipToPadding(false);
            postsRecyclerView.setClipChildren(false);
            postsRecyclerView.setNestedScrollingEnabled(false);

            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setOnRefreshListener(this::loadUserPosts);
            }

            View retryButton = view.findViewById(R.id.retryButton);
            if (retryButton != null) {
                retryButton.setOnClickListener(v -> loadUserPosts());
            }

            posts = new ArrayList<>();
            postAdapter = new PostAdapter(getContext(), posts, this);
            if (postsRecyclerView != null) {
                postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                postsRecyclerView.setAdapter(postAdapter);
            }
        }
    }

    private void loadUserPosts() {
        showLoading();
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getMyPosts().enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    posts.clear();
                    posts.addAll(response.body());

                    // Update like counts for each post
                    for (int i = 0; i < posts.size(); i++) {
                        final int position = i;
                        updateLikeCount(posts.get(i), position);
                    }
                    checkLikeStatusForPosts();
                    postAdapter.notifyDataSetChanged();
                    showContent();
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                hideLoading();
                showError();
            }
        });
    }

    private void updateLikeCount(Post post, int position) {
        if (!isAdded()) return;

        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getLikeCount(post.getId()).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    post.setLikeCount(response.body());
                    postAdapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("MyPostsFragment", "Error updating like count: " + t.getMessage());
            }
        });
    }

    private void checkLikeStatusForPosts() {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
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
                    Log.e("MyPostsFragment", "Failed to check like status: " + t.getMessage());
                }
            });
        }
    }

    @Override
    public void onLikeClick(Post post, int position) {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.likePost(post.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    post.setLiked(!post.isLiked());
                    // Update like count immediately after like action
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

    @Override
    public void onCommentClick(Post post, int position) {
        Intent intent = new Intent(getContext(), CommentActivity.class);
        intent.putExtra("postId", post.getId());
        startActivity(intent);
    }

    @Override
    public void onMoreClick(Post post, int position) {
        PopupMenu popup = new PopupMenu(requireContext(),
                postsRecyclerView.findViewHolderForAdapterPosition(position)
                        .itemView.findViewById(R.id.moreButton));
        popup.inflate(R.menu.menu_post_options);
        popup.setOnMenuItemClickListener(item -> {
            // Handle menu item clicks
            return true;
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
}
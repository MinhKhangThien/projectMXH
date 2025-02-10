package com.example.projectmxh.screen;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.projectmxh.CommentActivity;
import com.example.projectmxh.Model.Post;
import com.example.projectmxh.Model.SavedPost;
import com.example.projectmxh.R;
import com.example.projectmxh.adapter.PostAdapter;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavePostsFragment extends Fragment implements PostAdapter.PostClickListener {
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> savedPosts;
    private View loadingView;
    private View errorView;
    private View emptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.saveposts, container, false);
        initViews(view);
        loadSavedPosts();
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        loadingView = view.findViewById(R.id.loadingView);
        errorView = view.findViewById(R.id.errorView);
        emptyView = view.findViewById(R.id.emptyView);

        savedPosts = new ArrayList<>();
        adapter = new PostAdapter(requireContext(), savedPosts, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.retryButton).setOnClickListener(v -> loadSavedPosts());
    }

    private void loadSavedPosts() {
        showLoading();
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getSavedPosts().enqueue(new Callback<List<SavedPost>>() {
            @Override
            public void onResponse(Call<List<SavedPost>> call, Response<List<SavedPost>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = new ArrayList<>();
                    for (SavedPost savedPost : response.body()) {
                        Post post = savedPost.getPost();
                        post.setSaved(true); // Mark as saved since it's in saved posts
                        posts.add(post);
                    }

                    savedPosts.clear();
                    savedPosts.addAll(posts);
                    checkLikeStatusForPosts();
                    updateCommentCountsForPosts();

                    if (savedPosts.isEmpty()) {
                        showEmptyState();
                    } else {
                        showContent();
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<List<SavedPost>> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("SavedPosts", "Failed to load saved posts", t);
                showError();
            }
        });
    }

    private void checkLikeStatusForPosts() {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        for (Post post : savedPosts) {
            apiService.checkPostLike(post.getId()).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        post.setLiked(response.body());
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Log.e("SavedPosts", "Failed to check like status", t);
                }
            });
        }
    }

    private void updateCommentCountsForPosts() {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        for (Post post : savedPosts) {
            apiService.getCommentCount(post.getId()).enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        post.setCommentCount(response.body());
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Log.e("SavedPosts", "Failed to get comment count", t);
                }
            });
        }
    }

    @Override
    public void onLikeClick(Post post, int position) {
        if (!isAdded()) return;

        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.likePost(post.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    post.setLiked(!post.isLiked());
                    updateLikeCount(post, position);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to like post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLikeCount(Post post, int position) {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getLikeCount(post.getId()).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    post.setLikeCount(response.body());
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e("SavedPosts", "Failed to update like count", t);
            }
        });
    }

    @Override
    public void onCommentClick(Post post, int position) {
        if (!isAdded()) return;
        Intent intent = new Intent(getContext(), CommentActivity.class);
        intent.putExtra("postId", post.getId());
        startActivity(intent);
    }

    @Override
    public void onMoreClick(Post post, int position) {
        if (!isAdded()) return;
        View anchor = recyclerView.findViewHolderForAdapterPosition(position)
                .itemView.findViewById(R.id.moreButton);

        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_post_options, popup.getMenu());
        popup.show();
    }

    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showError() {
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }
}

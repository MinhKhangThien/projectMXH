package com.example.projectmxh.screen;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class PostFragment extends Fragment implements PostAdapter.PostClickListener {
    private static final String TAG = "PostFragment";
    private String postId;
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> posts;
    private View loadingView;
    private View errorView;

    public static PostFragment newInstance(String postId) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        initViews(view);
        loadPost();
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        loadingView = view.findViewById(R.id.loadingView);
        errorView = view.findViewById(R.id.errorView);

        posts = new ArrayList<>();
        adapter = new PostAdapter(requireContext(), posts, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.backIcon).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        errorView.findViewById(R.id.retryButton).setOnClickListener(v -> loadPost());
    }

    private void loadPost() {
        showLoading();
        if (postId == null || postId.isEmpty()) {
            showError();
            return;
        }

        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getPostById(postId).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    posts.clear();
                    posts.add(response.body());
                    adapter.notifyDataSetChanged();
                    checkLikeStatus();
                    updateLikeCount();
                    showContent();
                } else {
                    showError();
                    Toast.makeText(getContext(),
                            "Failed to load post: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Failed to load post", t);
                showError();
                Toast.makeText(getContext(),
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLikeStatus() {
        if (posts.isEmpty()) return;
        Post post = posts.get(0);

        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.checkPostLike(post.getId()).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    post.setLiked(response.body());
                    adapter.notifyItemChanged(0);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(TAG, "Failed to check like status", t);
            }
        });
    }

    private void updateLikeCount() {
        if (posts.isEmpty()) return;
        Post post = posts.get(0);

        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getLikeCount(post.getId()).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    post.setLikeCount(response.body());
                    adapter.notifyItemChanged(0);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(TAG, "Failed to update like count", t);
            }
        });
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
                    adapter.notifyItemChanged(position);
                    updateLikeCount();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to like post", Toast.LENGTH_SHORT).show();
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

        // Only show edit/delete options if current user is post owner
        Menu menu = popup.getMenu();
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isOwner = response.body().getId().equals(post.getUser().getId());
                    menu.findItem(R.id.action_edit).setVisible(isOwner);
                    menu.findItem(R.id.action_delete).setVisible(isOwner);
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                Log.e(TAG, "Failed to get current user", t);
            }
        });

        popup.show();
    }

    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showError() {
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }


}

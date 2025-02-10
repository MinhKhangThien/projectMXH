package com.example.projectmxh;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmxh.CommentActivity;
import com.example.projectmxh.CreatePostActivity;
import com.example.projectmxh.R;
import com.example.projectmxh.adapter.PostAdapter;
import com.example.projectmxh.Model.Post;
import com.example.projectmxh.adapter.SearchResultAdapter;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.screen.MessageActivity;
import com.example.projectmxh.screen.OtherProfileFragment;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;
import com.google.android.material.button.MaterialButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment implements PostAdapter.PostClickListener {
    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> posts;
    private CircleImageView quickPostAvatar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActivityResultLauncher<Intent> createPostLauncher;
    private View loadingView;
    private View errorView;
    private MaterialButton retryButton;
    private EditText searchBar;
    private RecyclerView searchResultsList;
    private SearchResultAdapter searchAdapter;
    private List<AppUserDto> searchResults;
    private ImageView messageButton;

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

        // Initialize message button
        messageButton = view.findViewById(R.id.messageButton);
        messageButton.setOnClickListener(v -> navigateToMessages());

        // Initialize RecyclerView
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), posts, this);

        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postsRecyclerView.setAdapter(postAdapter);

        // Setup Quick Post Section with avatar
        View quickPostSection = view.findViewById(R.id.quickPostSection);
        quickPostAvatar = quickPostSection.findViewById(R.id.userImage);
        loadCurrentUserAvatar();
        quickPostSection.setOnClickListener(v -> navigateToCreatePost());

        // Initialize state views
        loadingView = view.findViewById(R.id.loadingView);
        errorView = view.findViewById(R.id.errorView);
        retryButton = view.findViewById(R.id.retryButton);

        searchBar = view.findViewById(R.id.searchBar);
        searchResultsList = view.findViewById(R.id.searchResultsList);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        searchResults = new ArrayList<>();
        searchAdapter = new SearchResultAdapter(searchResults, user -> {
            // Handle user click - navigate to profile
            OtherProfileFragment fragment = OtherProfileFragment.newInstance(user.getId().toString());
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .addToBackStack(null)
                    .commit();

            // Clear search
            searchBar.setText("");
            hideSearchResults();
        });

        searchResultsList.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsList.setAdapter(searchAdapter);
        setupSearchBar();

//        // Setup Toolbar
//        Toolbar toolbar = view.findViewById(R.id.toolbar);
//        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
//        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("");
    }

    private void navigateToMessages() {
        Intent intent = new Intent(getContext(), MessageActivity.class);
        startActivity(intent);
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable searchRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    if (s.length() > 0) {
                        performSearch(s.toString());
                    } else {
                        hideSearchResults();
                    }
                };
                handler.postDelayed(searchRunnable, 300); // Delay search for 300ms
            }
        });
    }

    private void performSearch(String query) {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.searchUsers(query).enqueue(new Callback<List<AppUserDto>>() {
            @Override
            public void onResponse(Call<List<AppUserDto>> call, Response<List<AppUserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchResults.clear();
                    searchResults.addAll(response.body());
                    searchAdapter.notifyDataSetChanged();

                    if (!searchResults.isEmpty()) {
                        showSearchResults();
                    } else {
                        hideSearchResults();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AppUserDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Error searching users", Toast.LENGTH_SHORT).show();
                hideSearchResults();
            }
        });
    }

    private void showSearchResults() {
        searchResultsList.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.GONE);
    }

    private void hideSearchResults() {
        searchResultsList.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    private void loadCurrentUserAvatar() {
        if (quickPostAvatar == null || !isAdded()) return; // Add null check

        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (!isAdded()) return; // Check if fragment is still attached

                if (response.isSuccessful() && response.body() != null) {
                    String avatarUrl = response.body().getProfilePicture();
                    if (avatarUrl != null && quickPostAvatar != null) {
                        Glide.with(requireContext())
                                .load(avatarUrl)
                                .placeholder(R.drawable.avatar)
                                .error(R.drawable.avatar)
                                .into(quickPostAvatar);
                    } else {
                        // Load default avatar if URL is null
                        Glide.with(requireContext())
                                .load(R.drawable.avatar)
                                .into(quickPostAvatar);
                    }
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("HomeFragment", "Failed to load user avatar", t);
                // Load default avatar on failure
                if (quickPostAvatar != null) {
                    Glide.with(requireContext())
                            .load(R.drawable.avatar)
                            .into(quickPostAvatar);
                }
            }
        });
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


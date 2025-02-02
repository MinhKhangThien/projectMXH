package com.example.projectmxh.screen;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.projectmxh.Model.Post;
import com.example.projectmxh.R;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.enums.AccountType;
import com.example.projectmxh.enums.FollowStatus;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtherProfileFragment extends Fragment {
    private String userId;
    private Button followButton, messageButton;
    private ImageView myPostsIcon, reelIcon;
    private View privateAccountSection, contentContainer;
    private TextView nameText, bioText, postCountText, followerCountText, followingCountText;
    private CircleImageView avatarImage;
    private Boolean isFollowed = false;
    private FollowStatus followStatus = null;
    private AccountType accountType = AccountType.PUBLIC;

    private View rootView;
    private boolean isFragmentAttached = false;

    private static final String DEFAULT_USER_ID = "1079f733-3525-4c29-ba52-750cb3d1bcd2";

    public static OtherProfileFragment newInstance(String userId) {
        OtherProfileFragment fragment = new OtherProfileFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_other_profile, container, false);
        userId = DEFAULT_USER_ID;
        initViews(rootView);
        setupNavigation();
        loadUserProfile(); // Load profile first
        checkFollowStatus();
        return rootView;
    }

    private void initViews(View view) {
        followButton = view.findViewById(R.id.followButton);
        messageButton = view.findViewById(R.id.messageButton);
        myPostsIcon = rootView.findViewById(R.id.myPostsIcon);
        reelIcon = rootView.findViewById(R.id.reelIcon);
        privateAccountSection = view.findViewById(R.id.privateAccountSection);
        contentContainer = view.findViewById(R.id.contentContainer);

        nameText = view.findViewById(R.id.name);
        bioText = view.findViewById(R.id.bio);
        avatarImage = view.findViewById(R.id.avatar);
        postCountText = view.findViewById(R.id.postCount);
        followerCountText = view.findViewById(R.id.followerCount);
        followingCountText = view.findViewById(R.id.followingCount);

        followButton.setOnClickListener(v -> handleFollowClick());
        messageButton.setOnClickListener(v -> handleMessageClick());
    }

    private void loadUserProfile() {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getUserById(userId).enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                    checkFollowStatus(); // Re-check follow status
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Failed to load profile",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkFollowStatus() {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.checkFollowStatus(userId).enqueue(new Callback<FollowStatus>() {
            @Override
            public void onResponse(Call<FollowStatus> call, Response<FollowStatus> response) {
                if (response.isSuccessful() && response.body() != null) {
                    followStatus = response.body();
                } else {
                    // Handle 500 error - not following
                    followStatus = null;
                }
                updateViewState();
            }

            @Override
            public void onFailure(Call<FollowStatus> call, Throwable t) {
                followStatus = null;
                updateViewState();
            }
        });
    }

    private void updateUI(AppUserDto user) {
        nameText.setText(user.getDisplayName());
        bioText.setText(user.getBio());
        accountType = user.getAccountType();

//        postCountText.setText(String.valueOf(user.getPostCount()));
//        followerCountText.setText(String.valueOf(user.getFollowerCount()));
//        followingCountText.setText(String.valueOf(user.getFollowingCount()));

        if (user.getProfilePicture() != null) {
            Glide.with(this)
                    .load(user.getProfilePicture())
                    .placeholder(R.drawable.avatar)
                    .into(avatarImage);
        }
    }

    private void updateViewState() {
        if (!isFragmentAttached) return;

        // Reset all views first
        myPostsIcon.setVisibility(View.GONE);
        reelIcon.setVisibility(View.GONE);
        messageButton.setVisibility(View.GONE);
        privateAccountSection.setVisibility(View.GONE);
        contentContainer.setVisibility(View.GONE);

        // Case 1: Following and accepted
        if (followStatus == FollowStatus.ACCEPTED) {
            followButton.setText("Unfollow");
            messageButton.setVisibility(View.VISIBLE);
            myPostsIcon.setVisibility(View.VISIBLE);
            reelIcon.setVisibility(View.VISIBLE);
            contentContainer.setVisibility(View.VISIBLE);
            loadUserPostsFragment();
            return;
        }

        // Case 2: Pending follow request
        if (followStatus == FollowStatus.PENDING) {
            followButton.setText("Cancel Request");
            if (accountType == AccountType.PRIVATE) {
                privateAccountSection.setVisibility(View.VISIBLE);
            }
            return;
        }

        // Case 3: Not following or rejected
        followButton.setText("Follow");
        if (accountType == AccountType.PRIVATE) {
            privateAccountSection.setVisibility(View.VISIBLE);
        } else {
            // Public account
            messageButton.setVisibility(View.VISIBLE);
            myPostsIcon.setVisibility(View.VISIBLE);
            reelIcon.setVisibility(View.VISIBLE);
            contentContainer.setVisibility(View.VISIBLE);
            loadUserPostsFragment();
        }
    }

    private void handleFollowClick() {
        if (followStatus == null || followStatus == FollowStatus.REJECTED) {
            handleFollow();
        } else {
            handleUnfollow();
        }
    }

    private void handleFollow() {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.followUser(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    followStatus = accountType == AccountType.PRIVATE ?
                            FollowStatus.PENDING : FollowStatus.ACCEPTED;
                    updateViewState();
                    String message = accountType == AccountType.PRIVATE ?
                            "Follow request sent" : "Followed successfully";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to follow user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleUnfollow() {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.unfollowUser(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    followStatus = null;
                    updateViewState();
                    String message = followStatus == FollowStatus.PENDING ?
                            "Request canceled" : "Unfollowed successfully";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to unfollow user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleMessageClick() {
        // Navigate to message screen
    }

    private void setupNavigation() {
        myPostsIcon.setOnClickListener(v -> loadUserPostsFragment());
        reelIcon.setOnClickListener(v -> loadUserReelsFragment());
    }

    private void loadUserPostsFragment() {
        Log.d("OtherProfileFragment", "Loading posts for user: " + userId);
        UserPostsFragment fragment = UserPostsFragment.newInstance(userId);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.contentContainer, fragment)
                .commitAllowingStateLoss();
    }

    private void loadUserReelsFragment() {
        // TODO: Implement reels fragment
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        followButton.setOnClickListener(v -> handleFollowClick());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isFragmentAttached = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isFragmentAttached = false;
    }
}

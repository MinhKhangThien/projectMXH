package com.example.projectmxh.screen;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.projectmxh.R;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private View rootView;
    private CircleImageView avatar;
    private TextView nameText, bioText, postCount, followerCount, followingCount;
    private Fragment currentFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews();
        loadUserData();
        loadMyPostsFragment();
        return rootView;
    }

    private void initViews() {
        avatar = rootView.findViewById(R.id.avatar);
        nameText = rootView.findViewById(R.id.name);
        bioText = rootView.findViewById(R.id.bio);
        postCount = rootView.findViewById(R.id.postCount);
        followerCount = rootView.findViewById(R.id.followerCount);
        followingCount = rootView.findViewById(R.id.followingCount);

        rootView.findViewById(R.id.myPostsIcon).setOnClickListener(v -> loadMyPostsFragment());
        rootView.findViewById(R.id.settingsIcon).setOnClickListener(v -> navigateSetting());
        rootView.findViewById(R.id.savedPostsIcon).setOnClickListener(v -> loadSavedPostsFragment());
    }


    private void updateUI(AppUserDto user) {
        if (!isAdded()) return;
        nameText.setText(user.getDisplayName());
        bioText.setText(user.getBio());


        if (user.getProfilePicture() != null) {
            Glide.with(this)
                    .load(user.getProfilePicture())
                    .placeholder(R.drawable.avatar)
                    .into(avatar);
        }
    }

    private void loadUserData() {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AppUserDto user = response.body();
                    updateUI(user);
                    // Load counts after getting user data
                    loadUserCounts(user.getId().toString());
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadUserCounts(String userId) {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);

        // Load post count
        apiService.getUserPostCount(userId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    postCount.setText(String.valueOf(response.body()));
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                // Handle failure silently
            }
        });

        // Load followers count
        apiService.getFollowersCount(userId).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    followerCount.setText(String.valueOf(response.body()));
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                // Handle failure silently
            }
        });

        // Load following count
        apiService.getFollowingCount(userId).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    followingCount.setText(String.valueOf(response.body()));
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                // Handle failure silently
            }
        });
    }

    private void loadMyPostsFragment() {
        if (!isAdded()) return;

        currentFragment = new MyPostsFragment();
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.container, currentFragment)
                .commit();
    }

    private void navigateSetting() {
        if (!isAdded()) return;
        // Clear current child fragment before navigating
        if (currentFragment != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .remove(currentFragment)
                    .commit();
        }

        SettingsFragment settingsFragment = new SettingsFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, settingsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadSavedPostsFragment() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new SavePostsFragment())
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload MyPostsFragment when returning from settings
        if (currentFragment == null) {
            loadMyPostsFragment();
        }
    }
}

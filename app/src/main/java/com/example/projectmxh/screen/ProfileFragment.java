package com.example.projectmxh.screen;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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
        rootView.findViewById(R.id.settingsIcon).setOnClickListener(v -> navigateSettingActivity());
        rootView.findViewById(R.id.savedPostsIcon).setOnClickListener(v -> loadSavedPostsFragment());
    }

    private void loadUserData() {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AppUserDto user = response.body();
                    updateUI(user);
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(AppUserDto user) {
        nameText.setText(user.getDisplayName());
        bioText.setText(user.getBio());
//        postCount.setText(String.valueOf(user.getPostCount()));
//        followerCount.setText(String.valueOf(user.getFollowerCount()));
//        followingCount.setText(String.valueOf(user.getFollowingCount()));

        if (user.getProfilePicture() != null) {
            Glide.with(this)
                    .load(user.getProfilePicture())
                    .placeholder(R.drawable.avatar)
                    .into(avatar);
        }
    }

    private void loadMyPostsFragment() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new MyPostsFragment())
                .commit();
    }

    private void navigateSettingActivity() {
        Intent intent = new Intent(getContext(), SettingsActivity.class);
        startActivity(intent);
    }

    private void loadSavedPostsFragment() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new SavePostsFragment())
                .commit();
    }
}

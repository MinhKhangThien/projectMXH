package com.example.projectmxh;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.adapter.PostAdapter;
import com.example.projectmxh.adapter.PostAdapter;
import com.example.projectmxh.Model.Post;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment implements PostAdapter.PostClickListener {
    private View rootView;
    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> posts;
    private boolean isCurrentUser = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViews();
        loadUserData();
        setupPosts();
    }

    private void setupViews() {
        postsRecyclerView = rootView.findViewById(R.id.postsRecyclerView);
        MaterialButton actionButton = rootView.findViewById(R.id.actionButton);
        View quickPostSection = rootView.findViewById(R.id.quickPostSection);

        quickPostSection.setVisibility(isCurrentUser ? View.VISIBLE : View.GONE);

        if (isCurrentUser) {
            actionButton.setText("Edit Profile");
            //actionButton.setOnClickListener(v -> startEditProfile());
        } else {
            actionButton.setText("Follow");
            //actionButton.setOnClickListener(v -> toggleFollow());
        }

        quickPostSection.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreatePostActivity.class);
            startActivityForResult(intent, CREATE_POST_REQUEST);
        });
    }

    private static final int CREATE_POST_REQUEST = 100;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_POST_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            loadPosts();
        }
    }

    private void loadUserData() {
        // TODO: Load user data from API
        TextView userName = rootView.findViewById(R.id.userName);
        TextView userBio = rootView.findViewById(R.id.userBio);
        TextView postCount = rootView.findViewById(R.id.postCount);
        TextView followersCount = rootView.findViewById(R.id.followersCount);
        TextView followingCount = rootView.findViewById(R.id.followingCount);

        userName.setText("Nguyễn Nhật Khang");
        userBio.setText("Android Developer");
        postCount.setText("10");
        followersCount.setText("100");
        followingCount.setText("50");
    }

    private void setupPosts() {
        postsRecyclerView = rootView.findViewById(R.id.postsRecyclerView);
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), posts, this);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Use getContext()
        postsRecyclerView.setAdapter(postAdapter);

        loadPosts();
    }

    private void loadPosts() {
        // TODO: Load posts from API
        posts.clear();
        // Add dummy data
        postAdapter.notifyDataSetChanged();
    }

    // Implement PostClickListener methods
    @Override
    public void onLikeClick(Post post, int position) {
//        post.setLiked(!post.isLiked());
//        postAdapter.notifyItemChanged(position);
    }

    @Override
    public void onCommentClick(Post post, int position) {
        Intent intent = new Intent(getActivity(), CommentActivity.class);
        intent.putExtra("postId", post.getId());
        startActivity(intent);
    }

    @Override
    public void onMoreClick(Post post, int position) {
        PopupMenu popup = new PopupMenu(getActivity(), rootView.findViewById(R.id.moreButton));
        popup.getMenuInflater().inflate(R.menu.menu_post_options, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            // Handle menu item clicks
            return true;
        });
        popup.show();
    }
}
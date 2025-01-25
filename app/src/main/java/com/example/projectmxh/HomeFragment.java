package com.example.projectmxh;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.projectmxh.adapters.PostAdapter;
import com.example.projectmxh.models.Post;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements PostAdapter.PostClickListener {
    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> posts;

    private static final int CREATE_POST_REQUEST = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Setup Toolbar
        setupToolbar(view);

        // Setup RecyclerView and other views
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        setupRecyclerView(view);
        setupQuickPost(view);
        loadPosts("2");

        return view;
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerView(View view) {
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(posts, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        postsRecyclerView.setLayoutManager(layoutManager);

        // Add scroll listener for pagination
        postsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1)) {
                    loadMorePosts();
                }
            }
        });

        postsRecyclerView.setAdapter(postAdapter);
    }

    private void setupQuickPost(View view) {
        View quickPostSection = view.findViewById(R.id.quickPostSection);
        quickPostSection.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreatePostActivity.class);
            startActivityForResult(intent, CREATE_POST_REQUEST);
        });
    }

//    private void loadPosts(String userId) {
//
//        // TODO: Implement API call
//        // For testing, add dummy data
//        posts.add(new Post(
//                "1",
//                "user1",
//                "Nguyễn Nhật Khang",
//                String.valueOf(R.drawable.ic_avatar),
//                "This is a test post",
//                "https://hips.hearstapps.com/hmg-prod/images/dog-puppy-on-garden-royalty-free-image-1586966191.jpg?crop=1xw:0.99967xh;center,top&resize=1200:*",
//                System.currentTimeMillis(),
//                10,
//                5,
//                true
//        ));
//
//        posts.add(new Post(
//                "2",
//                "user1",
//                "Nguyễn Nhật Khang",
//                String.valueOf(R.drawable.ic_avatar),
//                "Beautiful scenes",
//                "https://images.pexels.com/photos/1557652/pexels-photo-1557652.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
//                System.currentTimeMillis(),
//                20,
//                3,
//                true
//        ));
//
//        posts.add(new Post(
//                "3",
//                "user1",
//                "Nguyễn Nhật Khang",
//                String.valueOf(R.drawable.ic_avatar),
//                "Paris",
//                "https://media.tacdn.com/media/attractions-splice-spp-674x446/07/03/1c/9c.jpg",
//                System.currentTimeMillis(),
//                30,
//                7,
//                true
//        ));
//
//        postAdapter.notifyDataSetChanged();
//    }

    private void loadPosts(String userId) {

        // TODO: Implement API call
//        ApiService apiService = ApiClient.getClient().create(ApiService.class);
//        apiService.getPosts(userId).enqueue(new Callback<List<Post>>() {
//            @Override
//            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    posts.clear();
//                    posts.addAll(response.body());
//                    postAdapter.notifyDataSetChanged();
//                } else {
//                    Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Post>> call, Throwable t) {
//                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });

        // For testing, add dummy data
        posts.add(new Post(
                "1",
                "user1",
                "Nguyễn Nhật Khang",
                String.valueOf(R.drawable.ic_avatar),
                "This is a test post",
                "https://hips.hearstapps.com/hmg-prod/images/dog-puppy-on-garden-royalty-free-image-1586966191.jpg?crop=1xw:0.99967xh;center,top&resize=1200:*",
                System.currentTimeMillis(),
                10,
                5,
                true
        ));

        posts.add(new Post(
                "2",
                "user1",
                "Nguyễn Nhật Khang",
                String.valueOf(R.drawable.ic_avatar),
                "Beautiful scenes",
                "https://images.pexels.com/photos/1557652/pexels-photo-1557652.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                System.currentTimeMillis(),
                20,
                3,
                true
        ));

        posts.add(new Post(
                "3",
                "user1",
                "Nguyễn Nhật Khang",
                String.valueOf(R.drawable.ic_avatar),
                "Paris",
                "https://media.tacdn.com/media/attractions-splice-spp-674x446/07/03/1c/9c.jpg",
                System.currentTimeMillis(),
                30,
                7,
                true
        ));

        postAdapter.notifyDataSetChanged();
    }

    private void loadMorePosts() {
        // TODO: Implement pagination
    }

    @Override
    public void onLikeClick(Post post, int position) {
        post.setLiked(!post.isLiked());
        // TODO: Update like status on server
        postAdapter.notifyItemChanged(position);
    }

    @Override
    public void onCommentClick(Post post, int position) {
        Intent intent = new Intent(getActivity(), CommentActivity.class);
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
}


package com.example.projectmxh.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmxh.Model.Post;
import com.example.projectmxh.R;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Post> posts;
    private PostClickListener listener;

    public interface PostClickListener {
        void onLikeClick(Post post, int position);
        void onCommentClick(Post post, int position);
        void onMoreClick(Post post, int position);
    }

    public PostAdapter(Context context, List<Post> posts, PostClickListener listener) {
        this.context = context;
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item_layout, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatarImage;
        private TextView authorName;
        private TextView postTime;
        private TextView postContent;
        private ImageView postImage;
        private ImageButton likeButton;
        private ImageButton commentButton;
        private ImageButton saveButton;
        private ImageButton moreButton;

        private TextView likeCountText;
        private TextView commentCountText;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.avatarImage);
            authorName = itemView.findViewById(R.id.nameText);
            postTime = itemView.findViewById(R.id.timeText);
            postContent = itemView.findViewById(R.id.postContentText);
            postImage = itemView.findViewById(R.id.postImage);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            saveButton = itemView.findViewById(R.id.saveButton);
            moreButton = itemView.findViewById(R.id.moreButton);

            likeCountText = itemView.findViewById(R.id.likeCountText);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentCountText = itemView.findViewById(R.id.commentCountText);

            setupClickListeners();
        }

        private void setupClickListeners() {
            likeButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onLikeClick(posts.get(position), position);
                }
            });

            commentButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onCommentClick(posts.get(position), position);
                }
            });

            saveButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Post post = posts.get(position);
                    toggleSavePost(post);
                }
            });

            moreButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onMoreClick(posts.get(position), position);
                }
            });
        }

        void bind(Post post) {
            // Set user info
            if (post.getUser() != null) {
                // Display name instead of username
                authorName.setText(post.getUser().getDisplayName());

                // Load user profile picture
                if (post.getUser().getProfilePicture() != null) {
                    Glide.with(avatarImage.getContext())
                            .load(post.getUser().getProfilePicture())
                            .placeholder(R.drawable.ic_avatar)
                            .error(R.drawable.ic_avatar)
                            .into(avatarImage);
                } else {
                    Glide.with(avatarImage.getContext())
                            .load(R.drawable.ic_avatar)
                            .into(avatarImage);
                }
            }

            // Set post content
            postContent.setText(post.getCaption());

            // Format and set time
            if (post.getCreatedAt() != null) {
                try {
                    String dateStr = post.getCreatedAt().replace('T', ' ');
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date date = format.parse(dateStr.split("\\.")[0]);

                    if (date != null) {
                        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                                date.getTime(),
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS
                        );
                        postTime.setText(timeAgo);
                    }
                } catch (ParseException e) {
                    postTime.setText(post.getCreatedAt());
                }
            }

            // Handle post image
            if (post.getPostContentUrl() == null) {
                post.setPostContentUrl(post.getPostContent()); // Use postContent if postContentUrl is null
            }
            String contentUrl = post.getPostContentUrl();
            if (contentUrl != null && !contentUrl.isEmpty()) {
                postImage.setVisibility(View.VISIBLE);
                Glide.with(postImage.getContext())
                        .load(contentUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(postImage);
            } else {
                postImage.setVisibility(View.GONE);
            }

            // Set like count and state
            likeCountText.setText(String.valueOf(post.getLikeCount()));
            updateLikeButtonState(post.isLiked());
            updateCommentCount(post);

            // Update save button state
            updateSaveButtonState(post.isSaved());
        }

        private void updateCommentCount(Post post) {
            ApiService apiService = ApiClient.getClientWithToken(context).create(ApiService.class);
            apiService.getCommentCount(post.getId()).enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        post.setCommentCount(response.body());
                        commentCountText.setText(String.valueOf(response.body()));
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    commentCountText.setText("0");
                }
            });
        }

        private void updateLikeButtonState(boolean isLiked) {
            likeButton.setImageResource(isLiked ?
                    R.drawable.ic_heart_filled :
                    R.drawable.ic_like);

            likeButton.setColorFilter(isLiked ?
                    context.getColor(R.color.red) :
                    context.getColor(R.color.black));
        }

        private String formatTimeAgo(String dateStr) {
            if (dateStr == null) return "Unknown";
            // TODO: Implement proper date formatting
            return "Just now";
        }

        private void toggleSavePost(Post post) {
            ApiService apiService = ApiClient.getClientWithToken(context).create(ApiService.class);
            Call<Void> call;

            if (post.isSaved()) {
                call = apiService.unsavePost(post.getId());
            } else {
                call = apiService.savePost(post.getId());
            }

            Log.d("PostAdapter", "Attempting to " + (post.isSaved() ? "unsave" : "save") + " post: " + post.getId());

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        post.setSaved(!post.isSaved());
                        updateSaveButtonState(post.isSaved());
                        String message = post.isSaved() ? "Post saved" : "Post unsaved";
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        Log.d("PostAdapter", "Successfully " + message.toLowerCase());
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ?
                                    response.errorBody().string() : "Unknown error";
                            Log.e("PostAdapter", "Error: " + response.code() + " - " + errorBody);
                            Toast.makeText(context,
                                    "Failed to update save status: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Log.e("PostAdapter", "Error reading error body", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("PostAdapter", "Network error", t);
                    Toast.makeText(context,
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void updateSaveButtonState(boolean isSaved) {
            saveButton.setImageResource(isSaved ?
                    R.drawable.ic_bookmark_filled :
                    R.drawable.ic_bookmark);

            saveButton.setColorFilter(isSaved ?
                    context.getColor(R.color.black) :
                    context.getColor(R.color.gray));
        }
    }
}
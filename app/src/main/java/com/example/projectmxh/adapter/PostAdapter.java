package com.example.projectmxh.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmxh.Model.Post;
import com.example.projectmxh.R;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

//            saveButton.setOnClickListener(v -> {
//                int position = getAdapterPosition();
//                if (position != RecyclerView.NO_POSITION) {
//                    listener.onCommentClick(posts.get(position), position);
//                }
//            });


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
    }
}
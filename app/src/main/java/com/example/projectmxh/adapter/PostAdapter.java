package com.example.projectmxh.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmxh.R;
import com.example.projectmxh.models.Post;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts;
    private PostClickListener listener;

    public interface PostClickListener {
        void onLikeClick(Post post, int position);
        void onCommentClick(Post post, int position);
        void onMoreClick(Post post, int position);
    }

    public PostAdapter(List<Post> posts, PostClickListener listener) {
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
        private ImageView authorImage;
        private TextView authorName;
        private TextView postTime;
        private TextView postContent;
        private ImageView postImage;
        private ImageButton likeButton;
        private ImageButton commentButton;
        private ImageButton saveButton;
        private ImageButton moreButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            authorImage = itemView.findViewById(R.id.authorImage);
            authorName = itemView.findViewById(R.id.authorName);
            postTime = itemView.findViewById(R.id.postTime);
            postContent = itemView.findViewById(R.id.postContent);
            postImage = itemView.findViewById(R.id.postImage);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            saveButton = itemView.findViewById(R.id.saveButton);
            moreButton = itemView.findViewById(R.id.moreButton);

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
                    listener.onCommentClick(posts.get(position), position);
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
            authorName.setText(post.getAuthorName());
            postContent.setText(post.getContent());
            postTime.setText(formatTime(post.getTimestamp()));

            // Load author avatar
            Glide.with(authorImage.getContext())
                    .load(post.getAuthorAvatar())
                    .placeholder(R.drawable.ic_avatar)
                    .into(authorImage);

            // Load post image if exists
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                postImage.setVisibility(View.VISIBLE);
                Glide.with(postImage.getContext())
                        .load(post.getImageUrl())
                        .into(postImage);
            } else {
                postImage.setVisibility(View.GONE);
            }

            // Update like button state
            if (post.isLiked()) {
                likeButton.setImageTintList(ColorStateList.valueOf(itemView.getContext().getColor(R.color.primaryColor)));
            } else {
                likeButton.setImageTintList(ColorStateList.valueOf(itemView.getContext().getColor(R.color.textSecondary)));
            }
        }

        private String formatTime(long timestamp) {
            // Implement time formatting logic
            return "Just now"; // Placeholder
        }
    }
}
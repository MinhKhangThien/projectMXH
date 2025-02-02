package com.example.projectmxh.adapter;

import android.content.Context;
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
import com.example.projectmxh.Model.Post;
import com.example.projectmxh.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

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
            authorName.setText(post.getUser().getUserName());
            postContent.setText(post.getCaption());
            postTime.setText(formatTimeAgo(post.getCreatedAt()));

            // Load user avatar (placeholder for now)
            Glide.with(avatarImage.getContext())
                    .load(R.drawable.ic_avatar)
                    .into(avatarImage);

            // Load post image if exists
            if (post.getPostContentUrl() != null && !post.getPostContentUrl().isEmpty()
                    && post.getPostType().equals("IMAGE")) {
                postImage.setVisibility(View.VISIBLE);
                Glide.with(postImage.getContext())
                        .load(post.getPostContentUrl())
                        .into(postImage);
            } else {
                postImage.setVisibility(View.GONE);
            }

            // Set like count
            likeCountText.setText(String.valueOf(post.getLikeCount()));

            // Set like button state
            likeButton.setImageResource(post.isLiked() ?
                    R.drawable.ic_heart_filled :
                    R.drawable.ic_like);
            likeButton.setColorFilter(post.isLiked() ?
                    context.getColor(R.color.red) :
                    context.getColor(R.color.gray));
        }

        private String formatTimeAgo(String dateStr) {
            // TODO: Implement proper date formatting
            return "Just now";
        }
    }
}
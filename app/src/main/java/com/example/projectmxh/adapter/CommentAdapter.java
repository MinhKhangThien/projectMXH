package com.example.projectmxh.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.projectmxh.Model.Comment;
import com.example.projectmxh.R;
import com.example.projectmxh.interfaces.ReplyClickListener;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;
import com.example.projectmxh.utils.DateUtil;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private final List<Comment> comments;
    private final Context context;
    private final ReplyClickListener replyListener;

    public CommentAdapter(Context context, List<Comment> comments, ReplyClickListener replyListener) {
        this.context = context;
        this.comments = comments;
        this.replyListener = replyListener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void addComment(Comment comment) {
        comments.add(0, comment);
        notifyItemInserted(0);
    }

    public void updateComments(List<Comment> newComments) {
        comments.clear();
        comments.addAll(newComments);
        notifyDataSetChanged();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView avatarImage;
        private final TextView userName;
        private final TextView commentText;
        private final TextView timeText;
        private final TextView replyText;
        private final TextView likeCountText;
        private final ImageButton likeButton;

        private TextView viewRepliesText;
        private LinearLayout repliesContainer;
        private boolean repliesLoaded = false;

        public boolean isViewRepliesClicked = false;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.commentAvatarImage);
            userName = itemView.findViewById(R.id.commentUserName);
            commentText = itemView.findViewById(R.id.commentText);
            timeText = itemView.findViewById(R.id.commentTime);
            replyText = itemView.findViewById(R.id.replyText);
            likeCountText = itemView.findViewById(R.id.likeCountText);
            likeButton = itemView.findViewById(R.id.likeButton);
            viewRepliesText = itemView.findViewById(R.id.viewRepliesText);
            repliesContainer = itemView.findViewById(R.id.repliesContainer);
        }

        void bind(Comment comment) {
            userName.setText(comment.getUserName());
            commentText.setText(comment.getText());
            timeText.setText(DateUtil.formatRelativeTime(comment.getCreatedAt())); // Hiển thị thời gian dạng "2 ngày trước"

            // Số lượt thích
            likeCountText.setText(String.valueOf(comment.getLikeCount()));

            // Nút trái tim
            likeButton.setImageResource(comment.getLikeCount() > 0 ?
                    R.drawable.ic_heart_filled : R.drawable.ic_like);
            likeButton.setColorFilter(comment.getLikeCount() > 0 ?
                    context.getColor(R.color.red) : context.getColor(R.color.gray));

            // Avatar
            Glide.with(context)
                    .load(comment.getUserProfilePicture())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .into(avatarImage);

            updateLikeUI(comment);

            likeButton.setOnClickListener(v -> handleLikeClick(comment));

            // Bắt sự kiện Reply
            replyText.setOnClickListener(v -> {
                if (replyListener != null) {
                    replyListener.onReplyClick(comment);
                }
            });

            // Load reply count
            loadReplyCount(comment);

            if (viewRepliesText != null) {
                viewRepliesText.setOnClickListener(v -> {
                    if (!repliesLoaded) {
                        loadReplies(comment);
                    } else {
                        toggleRepliesVisibility();
                    }
                });
            }
        }

        private void updateLikeUI(Comment comment) {
            likeButton.setImageResource(comment.isLiked() ?
                R.drawable.ic_heart_filled :
                R.drawable.ic_like);

            likeButton.setColorFilter(comment.isLiked() ?
                context.getColor(R.color.red) :
                context.getColor(R.color.gray));
        }

        private void handleLikeClick(Comment comment) {
            // Debug logs
            Log.d("CommentAdapter", "Like clicked for comment: " + comment.getId());
            Log.d("CommentAdapter", "Current like status: " + comment.isLiked());

            comment.setLiked(!comment.isLiked());
            updateLikeUI(comment);

            ApiService apiService = ApiClient.getClientWithToken(context).create(ApiService.class);
            Log.d("CommentAdapter", "Calling like API for comment ID: " + comment.getId());
            apiService.likeComment(comment.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("CommentAdapter", "Like API response code: " + response.code());

                    if (response.isSuccessful()) {
                        Log.d("CommentAdapter", "Like successful");
                        updateLikeCount(comment);
                    } else {
                        Log.e("CommentAdapter", "Like failed: " + response.errorBody());
                        // Revert on failure
                        comment.setLiked(!comment.isLiked());
                        updateLikeUI(comment);
                        Toast.makeText(context,
                            "Failed to like comment: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("CommentAdapter", "Like network error: " + t.getMessage());
                    // Revert on failure
                    comment.setLiked(!comment.isLiked());
                    updateLikeUI(comment);
                    Toast.makeText(context, "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void updateLikeCount(Comment comment) {
            Log.d("CommentAdapter", "Updating like count for comment: " + comment.getId());

            ApiService apiService = ApiClient.getClientWithToken(context).create(ApiService.class);
            apiService.getCommentLikeCount(comment.getId()).enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        int newLikeCount = response.body();
                        Log.d("CommentAdapter", "New like count: " + newLikeCount);
                        comment.setLikeCount(newLikeCount);
                        likeCountText.setText(String.valueOf(newLikeCount));
                    } else {
                        Log.e("CommentAdapter", "Get like count failed: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Log.e("CommentAdapter", "Get like count error: " + t.getMessage());
                }
            });
        }

        private void loadReplyCount(Comment comment) {
            ApiService apiService = ApiClient.getClientWithToken(context).create(ApiService.class);
            apiService.getReplyCount(comment.getId()).enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        int replyCount = response.body();
                        if (replyCount > 0) {
                            viewRepliesText.setVisibility(View.VISIBLE);
                            viewRepliesText.setText("View " + replyCount + " replies");
                        } else {
                            viewRepliesText.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Log.e("CommentAdapter", "Error loading reply count: " + t.getMessage());
                }
            });
        }

        private void loadReplies(Comment comment) {
            ApiService apiService = ApiClient.getClientWithToken(context).create(ApiService.class);
            apiService.getCommentReplies(comment.getId()).enqueue(new Callback<List<Comment>>() {
                @Override
                public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        displayReplies(response.body());
                        repliesLoaded = true;
                    }
                }

                @Override
                public void onFailure(Call<List<Comment>> call, Throwable t) {
                    Log.e("CommentAdapter", "Error loading replies: " + t.getMessage());
                }
            });
        }

        private void displayReplies(List<Comment> replies) {
            repliesContainer.removeAllViews();
            for (Comment reply : replies) {
                View replyView = LayoutInflater.from(context)
                        .inflate(R.layout.comment_item, repliesContainer, false);
                // Bind reply data to view
                bindReplyView(replyView, reply);
                repliesContainer.addView(replyView);
            }
            repliesContainer.setVisibility(View.VISIBLE);
        }

        private void bindReplyView(View replyView, Comment reply) {
            CircleImageView avatarImage = replyView.findViewById(R.id.commentAvatarImage);
            TextView userName = replyView.findViewById(R.id.commentUserName);
            TextView commentText = replyView.findViewById(R.id.commentText);
            TextView timeText = replyView.findViewById(R.id.commentTime);
            ImageButton likeButton = replyView.findViewById(R.id.likeButton);
            TextView likeCountText = replyView.findViewById(R.id.likeCountText);

            userName.setText(reply.getUserName());
            commentText.setText(reply.getText());
            timeText.setText(DateUtil.formatRelativeTime(reply.getCreatedAt()));
            likeCountText.setText(String.valueOf(reply.getLikeCount()));

            // Load avatar
            Glide.with(context)
                    .load(reply.getUserProfilePicture())
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .into(avatarImage);

            // Set like button state
            likeButton.setImageResource(reply.isLiked() ?
                    R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            likeButton.setColorFilter(reply.isLiked() ?
                    context.getColor(R.color.red) : context.getColor(R.color.gray));

            // Hide reply elements for replies
            replyView.findViewById(R.id.viewRepliesText).setVisibility(View.GONE);
            replyView.findViewById(R.id.repliesContainer).setVisibility(View.GONE);
        }

        private void toggleRepliesVisibility() {
            boolean isVisible = repliesContainer.getVisibility() == View.VISIBLE;
            repliesContainer.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        }


    }
}
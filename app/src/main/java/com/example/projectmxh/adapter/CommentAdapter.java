package com.example.projectmxh.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.projectmxh.R;
import com.example.projectmxh.models.Comment;
import de.hdodenhof.circleimageview.CircleImageView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private SimpleDateFormat dateFormat;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
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
        holder.bind(comments.get(position));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void addComment(Comment comment) {
        comments.add(0, comment);
        notifyItemInserted(0);
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView userImage;
        private TextView userName;
        private TextView commentText;
        private TextView timeText;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            commentText = itemView.findViewById(R.id.commentText);
            timeText = itemView.findViewById(R.id.timeText);
        }

        void bind(Comment comment) {
            userName.setText(comment.getUserName());
            commentText.setText(comment.getContent());
            timeText.setText(dateFormat.format(comment.getTimestamp()));

            Glide.with(userImage.getContext())
                    .load(comment.getUserAvatar())
                    .placeholder(R.drawable.ic_avatar)
                    .into(userImage);
        }
    }
}
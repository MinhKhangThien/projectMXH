package com.example.projectmxh;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.adapters.CommentAdapter;
import com.example.projectmxh.models.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentActivity extends AppCompatActivity {
    private String postId;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> comments;
    private EditText commentInput;
    private ImageButton sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        postId = getIntent().getStringExtra("postId");
        setupToolbar();
        setupViews();
        loadComments();
    }

    private void setupViews() {
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentInput = findViewById(R.id.commentInput);
        sendButton = findViewById(R.id.sendButton);

        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(comments);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);

        sendButton.setOnClickListener(v -> submitComment());
    }

    private void submitComment() {
        String content = commentInput.getText().toString().trim();
        if (content.isEmpty()) return;

        // TODO: Replace with actual user data and API call
        Comment newComment = new Comment(
            UUID.randomUUID().toString(),
            "currentUserId",
            "Current User",
            "",
            content,
            System.currentTimeMillis()
        );

        commentAdapter.addComment(newComment);
        commentInput.setText("");
        commentsRecyclerView.smoothScrollToPosition(0);
    }

    private void loadComments() {
        // TODO: Implement API call to load comments
        // For now, add dummy data
        comments.add(new Comment(
            "1", "user1", "John Doe", "", 
            "Great post!", System.currentTimeMillis() - 3600000
        ));
        commentAdapter.notifyDataSetChanged();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Comments");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
package com.example.projectmxh;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.Model.Comment;
import com.example.projectmxh.adapter.CommentAdapter;
import com.example.projectmxh.dto.request.CommentRequest;
import com.example.projectmxh.interfaces.ReplyClickListener;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentActivity extends AppCompatActivity implements ReplyClickListener {
    private String postId;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> comments;
    private EditText commentInput;
    private ImageButton sendButton;

    private Comment replyingTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        String postIdStr = getIntent().getStringExtra("postId");
        postId = String.valueOf(UUID.fromString(postIdStr));

        setupToolbar();
        setupViews();
        loadComments();
    }

    private void setupViews() {
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentInput = findViewById(R.id.commentInput);
        sendButton = findViewById(R.id.sendButton);

        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, comments, this); // Pass context as first parameter
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);

        sendButton.setOnClickListener(v -> submitComment());
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

    private void loadComments() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getComments(UUID.fromString(postId)).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful()) {
                    Log.d("CommentsResponse", new Gson().toJson(response.body()));
                } else {
                    Log.e("CommentsError", "Response code: " + response.code());
                }
                if (response.isSuccessful() && response.body() != null) {
                    comments.clear();
                    comments.addAll(response.body()); // Đúng kiểu danh sách
                    commentAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CommentActivity.this,
                            "Failed to load comments: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Toast.makeText(CommentActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitNormalComment(String content) {
        CommentRequest request = new CommentRequest(content, postId);
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);

        apiService.addComment(request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    commentInput.setText("");
                    loadComments();
                } else {
                    Toast.makeText(CommentActivity.this,
                            "Failed to add comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(CommentActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReplyClick(Comment parentComment) {
        replyingTo = parentComment;
        commentInput.setHint("Reply to " + parentComment.getUserName());
        commentInput.requestFocus();
    }

    private void submitComment() {
        String content = commentInput.getText().toString().trim();
        if (content.isEmpty()) return;

        if (replyingTo != null) {
            submitReply(content);
        } else {
            submitNormalComment(content);
        }
    }

    private void submitReply(String content) {
        CommentRequest request = new CommentRequest(content, postId);
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);

        apiService.replyToComment(replyingTo.getId(), request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    resetReplyState();
                    loadComments();
                } else {
                    Toast.makeText(CommentActivity.this,
                            "Failed to add reply", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(CommentActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetReplyState() {
        commentInput.setText("");
        commentInput.setHint("Write a comment...");
        replyingTo = null;
    }
}
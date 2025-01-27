package com.example.projectmxh.screen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.R;
import com.example.projectmxh.adapter.MessageAdapter;
import com.example.projectmxh.Model.User;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    private RecyclerView messagesList;
    private MessageAdapter messageAdapter;
    private List<User> users; // Danh sách người đã chat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages);

        messagesList = findViewById(R.id.messages_list);
        users = new ArrayList<>();

        messageAdapter = new MessageAdapter(users, user -> {
            // Chuyển sang ChatActivity khi nhấn vào box chat
            Intent intent = new Intent(MessageActivity.this, ChatActivity.class);
            intent.putExtra("receiverName", user.getUserName());
            intent.putExtra("receiverFullName", user.getFullName());
            intent.putExtra("avatarUrl", "https://i.pinimg.com/474x/b7/14/a2/b714a2713d5d9259dab2a7c0b7df4ff9.jpg");
            startActivity(intent);
        });

        messagesList.setLayoutManager(new LinearLayoutManager(this));
        messagesList.setAdapter(messageAdapter);

        loadBoxChats();
    }

    private void loadBoxChats() {
        // Thêm dữ liệu mẫu
        users.add(new User("1", "khang@gmail.com", "Loves technology", "Male", "Khang Nguyen"));
        users.add(new User("5", "khang48@gmail.com", "Loves technology", "Male", "Khang 2"));
        users.add(new User("2", "linhtran@gmail.com", "Dreamer and coder", "Female", "Linh Tran"));
        users.add(new User("3", "ha@gmail.com", "Full-stack developer", "Male", "Ha Vo"));
        users.add(new User("4", "thuynguyen@gmail.com", "Travel enthusiast", "Female", "Thuy Nguyen"));

        // Gán adapter để hiển thị dữ liệu
        messageAdapter.notifyDataSetChanged();
    }

    // API để lấy danh sách người đã chat
//    private void loadBoxChats() {
//        // API giả định để lấy danh sách người đã chat
//        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
//        apiService.getUsersList().enqueue(new Callback<List<User>>() {
//            @Override
//            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    users.clear();
//                    users.addAll(response.body());
//                    messageAdapter.notifyDataSetChanged();
//                } else {
//                    Toast.makeText(MessageActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<User>> call, Throwable t) {
//                Toast.makeText(MessageActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}

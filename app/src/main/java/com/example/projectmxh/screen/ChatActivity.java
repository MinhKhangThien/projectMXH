package com.example.projectmxh.screen;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.R;
import com.example.projectmxh.adapter.ChatAdapter;
import com.example.projectmxh.Model.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        // Tạo danh sách tin nhắn
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("Hello!", true));  // Tin nhắn gửi
        messages.add(new Message("Hi, khỏe không you?", false));  // Tin nhắn nhận
        messages.add(new Message("Khỏe chứ bro", true));  // Tin nhắn gửi
        messages.add(new Message("OK", false));  // Tin nhắn nhận
        // Khởi tạo Adapter
        ChatAdapter chatAdapter = new ChatAdapter(messages);

        // Gán Adapter cho RecyclerView
        RecyclerView recyclerView = findViewById(R.id.chatMessages);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}


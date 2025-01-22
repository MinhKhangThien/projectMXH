package com.example.projectmxh.screen;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.Model.Message;
import com.example.projectmxh.R;
import com.example.projectmxh.adapter.ChatAdapter;

import java.util.ArrayList;
import java.util.List;

public class DetailChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_chat);


    }
}
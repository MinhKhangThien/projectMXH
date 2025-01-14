package com.example.projectmxh.screen;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.example.projectmxh.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private ImageView myPostsIcon, savedPostsIcon, settingsIcon;
    private TextView nameTextView, bioTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);  // Layout chính chứa các biểu tượng

        // Ánh xạ các view
        myPostsIcon = findViewById(R.id.myPostsIcon);
        savedPostsIcon = findViewById(R.id.savedPostsIcon);
        settingsIcon = findViewById(R.id.settingsIcon);
        nameTextView = findViewById(R.id.name);
        bioTextView = findViewById(R.id.bio);

        // Đặt sự kiện click cho các icon
        myPostsIcon.setOnClickListener(v -> loadFragment(new MyPostsFragment()));
        savedPostsIcon.setOnClickListener(v -> loadFragment(new SavePostsFragment()));
        settingsIcon.setOnClickListener(v -> loadFragment(new SettingsFragment()));

        // Hiển thị nội dung mặc định trong FrameLayout
        if (savedInstanceState == null) {
            loadFragment(new MyPostsFragment()); // Mặc định hiển thị My Posts
        }
    }


    // Hàm để thay đổi Fragment trong FrameLayout
    private void loadFragment(androidx.fragment.app.Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}

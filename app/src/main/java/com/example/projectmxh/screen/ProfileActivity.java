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
    private FirebaseFirestore firestore;

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

        // Khởi tạo Firestore
        firestore = FirebaseFirestore.getInstance();

        // Lấy userID từ SharedPreferences
        String userId = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        if (userId != null) {
            loadUserData(userId);
        } else {
            Toast.makeText(this, "No user ID found", Toast.LENGTH_SHORT).show();
        }

        // Đặt sự kiện click cho các icon
        myPostsIcon.setOnClickListener(v -> loadFragment(new MyPostsFragment()));
        savedPostsIcon.setOnClickListener(v -> loadFragment(new SavePostsFragment()));
        settingsIcon.setOnClickListener(v -> loadFragment(new SettingsFragment()));

        // Hiển thị nội dung mặc định trong FrameLayout
        if (savedInstanceState == null) {
            loadFragment(new MyPostsFragment()); // Mặc định hiển thị My Posts
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Tải lại dữ liệu mỗi khi Activity được hiển thị lại
        String userId = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getString("userId", null);

        if (userId != null) {
            loadUserData(userId);
        }
    }

    // Hàm lấy dữ liệu người dùng từ Firestore
    private void loadUserData(String userId) {
        firestore.collection("users").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = document.getString("name");
                                String bio = document.getString("bio");

                                // Hiển thị thông tin người dùng
                                nameTextView.setText(name != null ? name : "No name available");
                                bioTextView.setText(bio != null ? bio : "No bio available");
                            } else {
                                Toast.makeText(ProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Hàm để thay đổi Fragment trong FrameLayout
    private void loadFragment(androidx.fragment.app.Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);  // Thay thế fragment vào FrameLayout
        transaction.addToBackStack(null);  // Cho phép quay lại fragment trước đó
        transaction.commit();
    }
}

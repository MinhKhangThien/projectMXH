package com.example.projectmxh;

import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {
    protected BottomNavigationView bottomNavigationView;
    protected FrameLayout contentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        contentFrame = findViewById(R.id.content_frame);
        
        setupBottomNavigation();
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            } else if (itemId == R.id.nav_notifications) {
                //loadFragment(new NotificationsFragment());
                return true;
            } else if (itemId == R.id.nav_menu) {
                //loadFragment(new MenuFragment());
                return true;
            }
            return false;
        });
    }

    protected void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit();
    }
}
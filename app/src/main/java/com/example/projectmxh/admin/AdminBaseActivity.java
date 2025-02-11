package com.example.projectmxh.admin;

import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.projectmxh.R;
import com.example.projectmxh.screen.ReportsFragment;
import com.example.projectmxh.screen.UserManagementContainerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminBaseActivity extends AppCompatActivity {
    protected BottomNavigationView bottomNavigationView;
    protected FrameLayout contentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_base);

        bottomNavigationView = findViewById(R.id.adminBottomNavigation);
        contentFrame = findViewById(R.id.admin_content_frame);

        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new UserManagementContainerFragment());
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_reports) {
                loadFragment(new ReportsFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_users) {
                loadFragment(new UserManagementContainerFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                loadFragment(new AdminSettingsFragment());
                return true;
            }
            return false;
        });
    }

    protected void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_content_frame, fragment)
                .commit();
    }
}
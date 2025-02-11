package com.example.projectmxh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.projectmxh.screen.ChatActivity;
import com.example.projectmxh.screen.NotificationsFragment;
import com.example.projectmxh.screen.OtherProfileFragment;
import com.example.projectmxh.screen.ProfileFragment;
import com.example.projectmxh.screen.SettingsFragment;
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

        // Check if we should load OtherProfileFragment
        if (getIntent().hasExtra("loadFragment") &&
                "otherProfile".equals(getIntent().getStringExtra("loadFragment"))) {
            String userId = getIntent().getStringExtra("userId");
            if (userId != null) {
                // Load the fragment
                loadFragment(OtherProfileFragment.newInstance(userId));

                // Don't set home as selected if coming from chat
                if (!getIntent().getBooleanExtra("fromChat", false)) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                }
            }
        } else {
            // Load default fragment
            if (savedInstanceState == null) {
                loadFragment(new HomeFragment());
            }
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
                loadFragment(new NotificationsFragment());
                return true;
            } else if (itemId == R.id.nav_menu) {
                loadFragment(new SettingsFragment());
                return true;
            }
            return false;
        });
    }

    protected void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if (currentFragment instanceof OtherProfileFragment &&
                getIntent().getBooleanExtra("fromChat", false)) {
            // Return to ChatActivity
            Intent intent = new Intent(this, ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }


}
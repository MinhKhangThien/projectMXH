package com.example.projectmxh.screen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.projectmxh.R;
import com.example.projectmxh.adapter.UserManagementPagerAdapter;
import com.example.projectmxh.interfaces.UserManagementRefreshListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class UserManagementContainerFragment extends Fragment implements UserManagementRefreshListener {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private UserManagementPagerAdapter pagerAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management_container, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        pagerAdapter = new UserManagementPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText(position == 0 ? "Active Users" : "Banned Users");
                }
        );
        tabLayoutMediator.attach();

        return view;
    }


    @Override
    public void onUserStatusChanged() {
        // Get fragments directly from adapter
        Fragment activeFragment = pagerAdapter.getFragment(0);
        Fragment bannedFragment = pagerAdapter.getFragment(1);

        if (activeFragment instanceof ActiveUsersFragment) {
            ((ActiveUsersFragment) activeFragment).loadActiveUsers();
        }

        if (bannedFragment instanceof BannedUsersFragment) {
            ((BannedUsersFragment) bannedFragment).loadBannedUsers();
        }
    }

    public void refreshCurrentTab() {
        Fragment currentFragment = pagerAdapter.getFragment(viewPager.getCurrentItem());
        if (currentFragment instanceof ActiveUsersFragment) {
            ((ActiveUsersFragment) currentFragment).loadActiveUsers();
        } else if (currentFragment instanceof BannedUsersFragment) {
            ((BannedUsersFragment) currentFragment).loadBannedUsers();
        }
    }
}

package com.example.projectmxh.adapter;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.projectmxh.screen.ActiveUsersFragment;
import com.example.projectmxh.screen.BannedUsersFragment;

public class UserManagementPagerAdapter extends FragmentStateAdapter {
    private final Fragment[] fragments = new Fragment[2];

    public UserManagementPagerAdapter(Fragment fragment) {
        super(fragment);
        fragments[0] = new ActiveUsersFragment();
        fragments[1] = new BannedUsersFragment();
    }

    @Override
    public Fragment createFragment(int position) {
        return fragments[position];
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }

    public Fragment getFragment(int position) {
        return fragments[position];
    }
}

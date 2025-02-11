package com.example.projectmxh.admin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.projectmxh.R;
import com.example.projectmxh.screen.SigninActivity;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

public class AdminSettingsFragment extends Fragment {
    private TextView adminEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_settings, container, false);
        initializeViews(view);
        loadAdminInfo();
        return view;
    }

    private void initializeViews(View view) {
        adminEmail = view.findViewById(R.id.adminEmail);
        Button logoutButton = view.findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void loadAdminInfo() {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getMe().enqueue(new retrofit2.Callback<com.example.projectmxh.dto.AppUserDto>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.projectmxh.dto.AppUserDto> call,
                                   retrofit2.Response<com.example.projectmxh.dto.AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adminEmail.setText(response.body().getUsername());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.projectmxh.dto.AppUserDto> call,
                                  Throwable t) {
                // Handle failure
            }
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", null)
                .show();
    }

    private void performLogout() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        prefs.edit().remove("auth_token").apply();

        Intent intent = new Intent(getContext(), SigninActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
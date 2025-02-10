package com.example.projectmxh.screen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.projectmxh.R;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.enums.AccountType;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {
    private View rootView;
    private Switch switchPrivacy;
    private Button btnLogout;
    private String currentUserId;

    private boolean isChangingAccountType = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.setting, container, false);
        initViews();
        getCurrentUser();
        return rootView;
    }

    private void initViews() {
        // Back button
        ImageView backIcon = rootView.findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Privacy switch
        switchPrivacy = rootView.findViewById(R.id.switchPrivacy);
        switchPrivacy.setEnabled(false); // Disable until we get user data
        switchPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChangingAccountType && currentUserId != null) {
                changeAccountType(isChecked);
            }
        });

        // Logout button
        btnLogout = rootView.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void getCurrentUser() {
        // Show loading state
        switchPrivacy.setEnabled(false);

        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    AppUserDto user = response.body();
                    currentUserId = user.getId();

                    // Set initial switch state based on account type
                    isChangingAccountType = true;
                    String accountType = String.valueOf(user.getAccountType());

                    // Explicitly check if account is PRIVATE
                    boolean isPrivate = AccountType.PRIVATE.name().equals(accountType);

                    // Update UI
                    switchPrivacy.setChecked(isPrivate);
                    switchPrivacy.setEnabled(true);
                    isChangingAccountType = false;

                    // Log for debugging
                    Log.d("SettingsFragment", "Account Type: " + accountType + ", Is Private: " + isPrivate);
                } else {
                    Toast.makeText(getContext(),
                            "Failed to load account settings",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                if (!isAdded()) return;

                switchPrivacy.setEnabled(true);
                Toast.makeText(getContext(),
                        "Failed to load user info",
                        Toast.LENGTH_SHORT).show();
                Log.e("SettingsFragment", "Error loading user: " + t.getMessage());
            }
        });
    }

    private void changeAccountType(boolean isPrivate) {
        if (currentUserId == null) {
            Toast.makeText(getContext(), "User ID not available", Toast.LENGTH_SHORT).show();
            revertSwitchState(!isPrivate);
            return;
        }

        String newType = isPrivate ? "PRIVATE" : "PUBLIC";
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);

        // Disable switch while request is in progress
        switchPrivacy.setEnabled(false);

        apiService.changeAccountType(currentUserId, newType).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) return;

                switchPrivacy.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(),
                            "Account is now " + newType.toLowerCase(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    revertSwitchState(!isPrivate);
                    String errorMessage = "Failed to change account type: " +
                            (response.code() == 403 ? "Permission denied" : "Server error");
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded()) return;

                switchPrivacy.setEnabled(true);
                revertSwitchState(!isPrivate);
                Toast.makeText(getContext(),
                        "Network error: Please try again",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void revertSwitchState(boolean state) {
        isChangingAccountType = true;
        switchPrivacy.setChecked(state);
        isChangingAccountType = false;
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
        // Clear token
        SharedPreferences sharedPreferences = requireContext()
                .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove("token").apply();

        // Navigate to login
        Intent intent = new Intent(getContext(), SigninActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
    }
}
package com.example.projectmxh.screen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectmxh.BaseActivity;
import com.example.projectmxh.R;
import com.example.projectmxh.admin.AdminBaseActivity;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.enums.RoleType;
import com.example.projectmxh.dto.request.LoginRequest;
import com.example.projectmxh.dto.response.LoginResponse;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SigninActivity extends AppCompatActivity {
    EditText email, pass;
    Button btnLogin;
    Button btnGoogleLogin;
    TextView signup;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);
        email = findViewById(R.id.et_email);
        pass = findViewById(R.id.et_password);
        signup = findViewById(R.id.tv_signup);

        btnLogin = findViewById(R.id.btn_login);
        btnGoogleLogin = findViewById(R.id.btn_google_login);

        // Khởi tạo Retrofit ApiService
        apiService = ApiClient.getClientWithoutToken().create(ApiService.class);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailInput = email.getText().toString().trim();
                String passwordInput = pass.getText().toString().trim();

                if (TextUtils.isEmpty(emailInput) || TextUtils.isEmpty(passwordInput)) {
                    Toast.makeText(SigninActivity.this, "Email and password can not be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    performLogin(emailInput, passwordInput);
                }
            }
        });

        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private void performLogin(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();

                    // Save token temporarily
                    getSharedPreferences("app_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("auth_token", token)
                            .apply();

                    // Check if user is banned
                    ApiService authenticatedApi = ApiClient.getClientWithToken(SigninActivity.this)
                            .create(ApiService.class);

                    authenticatedApi.getMe().enqueue(new Callback<AppUserDto>() {
                        @Override
                        public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                AppUserDto user = response.body();

                                // Check if user is banned
                                if (user.getIsBanned() != null && user.getIsBanned()) {
                                    // Remove the temporarily saved token
                                    getSharedPreferences("app_prefs", MODE_PRIVATE)
                                            .edit()
                                            .remove("auth_token")
                                            .apply();

                                    Toast.makeText(SigninActivity.this,
                                            "Your account has been banned. Please contact administrator.",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                RoleType userRole = user.getRole();

                                // Save user role and proceed with login
                                getSharedPreferences("app_prefs", MODE_PRIVATE)
                                        .edit()
                                        .putString("user_role", userRole.name())
                                        .apply();

                                Toast.makeText(SigninActivity.this,
                                        "Login successful!", Toast.LENGTH_SHORT).show();

                                // Navigate based on role
                                if (userRole == RoleType.ADMIN) {
                                    Intent intent = new Intent(SigninActivity.this,
                                            AdminBaseActivity.class);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(SigninActivity.this,
                                            BaseActivity.class);
                                    startActivity(intent);
                                }
                                finish();
                            } else {
                                // Remove the temporarily saved token
                                getSharedPreferences("app_prefs", MODE_PRIVATE)
                                        .edit()
                                        .remove("auth_token")
                                        .apply();

                                Toast.makeText(SigninActivity.this,
                                        "Failed to get user info", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<AppUserDto> call, Throwable t) {
                            // Remove the temporarily saved token
                            getSharedPreferences("app_prefs", MODE_PRIVATE)
                                    .edit()
                                    .remove("auth_token")
                                    .apply();

                            Toast.makeText(SigninActivity.this,
                                    "Network error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(SigninActivity.this,
                            "Login Failed! Invalid credentials.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(SigninActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}

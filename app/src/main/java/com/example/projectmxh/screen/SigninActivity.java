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
        // Gọi API login
        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Đăng nhập thành công
                    String token = response.body().getToken();

                    // Lưu token vào SharedPreferences
                    getSharedPreferences("app_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("auth_token", token)
                            .apply();

                    Toast.makeText(SigninActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển sang BaseActivity
                    Intent intent = new Intent(SigninActivity.this, BaseActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Xử lý lỗi từ server
                    Toast.makeText(SigninActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Xử lý lỗi kết nối
                Toast.makeText(SigninActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

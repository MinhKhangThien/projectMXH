package com.example.projectmxh.screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectmxh.MainActivity;
import com.example.projectmxh.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SigninActivity extends AppCompatActivity {
    EditText email, pass;
    Button btnLogin;
    TextView signup;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);
        email = findViewById(R.id.et_email);
        pass = findViewById(R.id.et_password);
        signup = findViewById(R.id.tv_signup);
        mAuth = FirebaseAuth.getInstance();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }
    private void loginUser() {
        String userEmail = email.getText().toString().trim();
        String userPass = pass.getText().toString().trim();

        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Email không được để trống");
            return;
        }
        if (TextUtils.isEmpty(userPass)) {
            pass.setError("Password không được để trống");
            return;
        }

        mAuth.signInWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userId = mAuth.getCurrentUser().getUid(); // Lấy userID của người dùng hiện tại

                    // Lưu userID vào SharedPreferences
                    getSharedPreferences("AppPrefs", MODE_PRIVATE)
                            .edit()
                            .putString("userId", userId)
                            .apply();

                    // Chuyển sang ProfileActivity
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SigninActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }
}
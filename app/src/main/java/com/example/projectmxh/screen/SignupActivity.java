package com.example.projectmxh.screen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectmxh.R;
import com.example.projectmxh.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etPasswordConfirm;
    private CheckBox checkboxTerms;
    private Button btnContinue;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // Khởi tạo FirebaseAuth và Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ các view
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_passwordconfirm);
        checkboxTerms = findViewById(R.id.checkbox_terms);
        btnContinue = findViewById(R.id.btn_continue);

        // Sự kiện cho nút "Continue"
        btnContinue.setOnClickListener(v -> signUpWithEmailPassword());
    }

    private void signUpWithEmailPassword() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etPasswordConfirm.getText().toString().trim();

        // Kiểm tra email đã nhập
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }

        // Kiểm tra mật khẩu và xác nhận mật khẩu
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        if (confirmPassword.isEmpty()) {
            etPassword.setError("Confirm Password is required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra checkbox đồng ý điều khoản
        if (!checkboxTerms.isChecked()) {
            Toast.makeText(SignupActivity.this, "You must agree to the Terms and Privacy Policy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra email có tồn tại trong Firebase Authentication chưa
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getSignInMethods().isEmpty()) {
                            // Nếu email chưa tồn tại, thực hiện đăng ký
                            createAccount(email, password);
                        } else {
                            Toast.makeText(SignupActivity.this, "Email is already registered", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignupActivity.this, "Error checking email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng ký thành công
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Tạo các giá trị mặc định cho Name, Bio, và Avatar
                        String name = "User"; // Tên mặc định
                        String bio = "This is my bio"; // Bio mặc định
                        String avatar = ""; // Avatar mặc định là chuỗi rỗng

                        // Tạo đối tượng User với các giá trị đã gán
                        User newUser = new User(user.getUid(), email, avatar, name, bio);

                        // Lưu thông tin người dùng vào Firestore
                        db.collection("users")  // Tạo collection "users"
                                .document(user.getUid())  // Tạo document với userID
                                .set(newUser)  // Lưu thông tin người dùng vào Firestore
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SignupActivity.this, "Registration and Firestore setup successful", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SignupActivity.this, "Error adding user data to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                        // Chuyển sang màn hình đăng nhập
                        Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Đăng ký thất bại
                        Toast.makeText(SignupActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

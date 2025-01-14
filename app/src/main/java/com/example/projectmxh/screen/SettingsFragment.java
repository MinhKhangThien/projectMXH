package com.example.projectmxh.screen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projectmxh.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;  // Import Picasso

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private EditText fullNameEditText, bioEditText;
    private Button updateButton;
    private ImageView avatarImageView;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private String userId;
    private Uri avatarUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout for the fragment
        return inflater.inflate(R.layout.settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các view
        fullNameEditText = view.findViewById(R.id.fullName);
        bioEditText = view.findViewById(R.id.bio);
        updateButton = view.findViewById(R.id.btn_confirm);
        avatarImageView = view.findViewById(R.id.avatar);

        // Khởi tạo Firestore và FirebaseStorage
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        // Lấy userId từ SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        if (userId != null) {
            fetchUserData();
        } else {
            Toast.makeText(getContext(), "User ID not found", Toast.LENGTH_SHORT).show();
        }

        // Sự kiện click chọn ảnh
        avatarImageView.setOnClickListener(v -> openImagePicker());

        // Xử lý sự kiện click nút "Update"
        updateButton.setOnClickListener(v -> updateUserData());
    }

    // Hàm lấy dữ liệu người dùng từ Firestore
    private void fetchUserData() {
        firestore.collection("users").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Gán dữ liệu vào EditText
                                fullNameEditText.setText(document.getString("name"));
                                bioEditText.setText(document.getString("bio"));

                                // Nếu có URL avatar, hiển thị ảnh
                                String avatarUrl = document.getString("avatar");
                                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                    // Thay Glide bằng Picasso
                                    Picasso.get().load(avatarUrl).into(avatarImageView);  // Sử dụng Picasso để tải và hiển thị ảnh
                                }
                            } else {
                                Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Mở ứng dụng chọn ảnh từ điện thoại
    private void openImagePicker() {
        // Sử dụng Intent để mở bộ chọn ảnh (trong ví dụ này có thể sử dụng Intent Gallery)
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    // Khi chọn ảnh xong, xử lý kết quả
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == getActivity().RESULT_OK && data != null) {
            avatarUri = data.getData(); // Lấy Uri của ảnh đã chọn
            if (avatarUri != null) {
                avatarImageView.setImageURI(avatarUri); // Hiển thị ảnh lên ImageView
                Log.d("SettingsFragment", "Avatar URI: " + avatarUri.toString()); // Kiểm tra URI
            }
        }
    }


    // Hàm cập nhật dữ liệu người dùng lên Firestore
    private void updateUserData() {
        String updatedName = fullNameEditText.getText().toString().trim();
        String updatedBio = bioEditText.getText().toString().trim();

        if (updatedName.isEmpty() || updatedBio.isEmpty()) {
            Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu có ảnh, upload lên Firebase Storage
        if (avatarUri != null) {
            uploadAvatarAndUpdateProfile(updatedName, updatedBio);
        } else {
            saveUserDataToFirestore(updatedName, updatedBio, null);
        }
    }

    // Hàm upload ảnh lên Firebase Storage và lưu URL vào Firestore
    private void uploadAvatarAndUpdateProfile(String name, String bio) {
        StorageReference avatarRef = firebaseStorage.getReference().child("avatars/" + userId + ".jpg");
        avatarRef.putFile(avatarUri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        avatarRef.getDownloadUrl().addOnCompleteListener(urlTask -> {
                            if (urlTask.isSuccessful()) {
                                String avatarUrl = urlTask.getResult().toString();
                                saveUserDataToFirestore(name, bio, avatarUrl);
                            } else {
                                // Lỗi khi lấy URL của ảnh từ Firebase Storage
                                String errorMessage = "Failed to get avatar URL: " + urlTask.getException().getMessage();
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                Log.e("SettingsFragment", errorMessage);
                            }
                        });
                    } else {
                        // Lỗi khi upload ảnh lên Firebase Storage
                        String errorMessage = "Failed to upload avatar: " + task.getException().getMessage();
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("SettingsFragment", errorMessage);
                    }
                });
    }


    // Hàm lưu thông tin người dùng vào Firestore
    private void saveUserDataToFirestore(String name, String bio, @Nullable String avatarUrl) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("bio", bio);
        if (avatarUrl != null) {
            userData.put("avatar", avatarUrl);  // Lưu URL ảnh vào trường "avatar"
        }

        firestore.collection("users").document(userId)
                .update(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                        // Quay lại màn hình trước đó
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

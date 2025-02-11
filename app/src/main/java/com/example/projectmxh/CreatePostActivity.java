package com.example.projectmxh;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmxh.Components.OptionAdapter;
import com.example.projectmxh.Components.OptionItem;
import com.example.projectmxh.config.CloudinaryConfig;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.dto.request.CreatePostRequest;
import com.example.projectmxh.dto.response.CloudinaryResponse;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePostActivity extends AppCompatActivity {
    private static final int RESULT_POST_CREATED = 101;
    public static final int REQUEST_CREATE_POST = 100;
    private CircleImageView profileImage;
    private TextView userNameText;

    private ProgressDialog progressDialog;

    // UI Components
    private EditText postContentEditText;
    private ImageView selectedImageView;
    private RecyclerView optionsRecyclerView;
    private View bottomSheetView;
    private Spinner privacySpinner;

    // Bottom Sheet
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private OptionAdapter optionAdapter;
    private List<OptionItem> optionList;

    // Image Picker
    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        initializeViews();
        setupToolbar();
        setupBottomSheet();
        setupPhotoPicker();
        setupListeners();
        loadUserProfile();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void initializeViews() {
        postContentEditText = findViewById(R.id.postContentEditText);
        selectedImageView = findViewById(R.id.selectedImageView);
        optionsRecyclerView = findViewById(R.id.optionsRecyclerView);
        bottomSheetView = findViewById(R.id.bottomSheet);
        privacySpinner = findViewById(R.id.privacySpinner);
        profileImage = findViewById(R.id.profileImage);
        userNameText = findViewById(R.id.userNameText);

        // Setup privacy spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.privacy_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        privacySpinner.setAdapter(adapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void loadUserProfile() {
        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AppUserDto user = response.body();
                    userNameText.setText(user.getDisplayName());
                    if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                        Glide.with(CreatePostActivity.this)
                                .load(user.getProfilePicture())
                                .placeholder(R.drawable.ic_avatar)
                                .error(R.drawable.ic_avatar)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                Toast.makeText(CreatePostActivity.this,
                        "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setHideable(true);

        optionList = getOptionList();
        optionAdapter = new OptionAdapter(optionList, this::handleOptionClick);
        optionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        optionsRecyclerView.setAdapter(optionAdapter);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
                // Optional: Handle slide events
            }
        });
    }

    private void setupPhotoPicker() {
        photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) {
                    selectedImageView.setImageURI(uri);
                    findViewById(R.id.imagePreviewCard).setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void setupListeners() {
        // Close button
        findViewById(R.id.closeButton).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        // Post button
        MaterialButton postButton = findViewById(R.id.postButton);
        postButton.setOnClickListener(v -> createPost());

        // Remove image button
        findViewById(R.id.removeImageButton).setOnClickListener(v -> {
            selectedImageView.setImageURI(null);
            findViewById(R.id.imagePreviewCard).setVisibility(View.GONE);
        });

        // Bottom sheet drag handle
        findViewById(R.id.dragHandle).setOnClickListener(v -> toggleBottomSheet());
    }

    private void toggleBottomSheet() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private List<OptionItem> getOptionList() {
        List<OptionItem> options = new ArrayList<>();
        options.add(new OptionItem(R.drawable.ic_image, "Add a Photo"));
        options.add(new OptionItem(R.drawable.ic_document, "Add a Document"));
        options.add(new OptionItem(R.drawable.ic_color, "Background Color"));
        options.add(new OptionItem(R.drawable.ic_camera_k, "Camera"));
        return options;
    }

    private void handleOptionClick(int position) {
        switch (position) {
            case 0: // Photo/Video
                photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
                break;
            case 1: // Camera
                // TODO: Implement camera functionality
                break;
            case 2: // Feeling/Activity
                // TODO: Implement feeling/activity selector
                break;
            case 3: // Check in
                // TODO: Implement location picker
                break;
        }
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void createPost() {
        String caption = postContentEditText.getText().toString().trim();
        
        if (caption.isEmpty() && selectedImageView.getDrawable() == null) {
            Toast.makeText(this, "Please add some content or image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        //showLoading();

        showProgressDialog("Creating post...");

        // First upload image to Cloudinary if image is selected
        if (selectedImageView.getDrawable() != null) {
            handleImagePost(caption);
        } else {
            handleTextPost(caption);
        }
    }

    private void handleImagePost(String caption) {
        File imageFile = convertImageViewToFile();
        if (imageFile == null) {
            hideProgressDialog();
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare upload
        RequestBody uploadPreset = RequestBody.create(
            MediaType.parse("text/plain"), 
            CloudinaryConfig.UPLOAD_PRESET
        );
        
        RequestBody requestFile = RequestBody.create(
            imageFile,
            MediaType.parse("image/*")
        );
        
        MultipartBody.Part body = MultipartBody.Part.createFormData(
            "file", 
            imageFile.getName(), 
            requestFile
        );

        // Upload to Cloudinary
        ApiService cloudinaryService = ApiClient.getClientWithoutToken().create(ApiService.class);
        cloudinaryService.uploadToCloudinary(uploadPreset, body)
            .enqueue(new Callback<CloudinaryResponse>() {
                @Override
                public void onResponse(Call<CloudinaryResponse> call, Response<CloudinaryResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String imageUrl = response.body().getUrl();
                        createPostWithData(caption, "IMAGE", imageUrl);
                    } else {
                        handleError("Failed to upload image");
                    }
                }

                @Override
                public void onFailure(Call<CloudinaryResponse> call, Throwable t) {
                    handleError("Network error: " + t.getMessage());
                }
            });
    }

    private void handleTextPost(String caption) {
        createPostWithData(caption, "REEL", null);
    }

    private void createPostWithData(String caption, String type, String imageUrl) {
        CreatePostRequest request = new CreatePostRequest(caption, type, imageUrl);

        ApiService apiService = ApiClient.getClientWithToken(this).create(ApiService.class);
        apiService.createPost(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                hideProgressDialog();
                if (response.isSuccessful()) {
                    Toast.makeText(CreatePostActivity.this,
                            "Post created successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e("CreatePost", "Error: " + errorBody);
                        handleError("Failed to create post: " + errorBody);
                    } catch (IOException e) {
                        Log.e("CreatePost", "Error reading error body", e);
                        handleError("Failed to create post");
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CreatePost", "Network error", t);
                handleError("Network error: " + t.getMessage());
            }
        });
    }

        private File convertImageViewToFile() {
        try {
            // Get drawable from ImageView
            Bitmap bitmap = ((BitmapDrawable) selectedImageView.getDrawable()).getBitmap();
            
            // Create a file to write bitmap data
            File f = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
            f.createNewFile();

            // Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            byte[] bitmapData = bos.toByteArray();

            // Write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            return f;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handleError(String message) {
        hideProgressDialog();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showProgressDialog(String message) {
        if (progressDialog != null) {
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
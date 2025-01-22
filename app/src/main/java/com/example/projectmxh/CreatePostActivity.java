package com.example.projectmxh;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.Components.OptionAdapter;
import com.example.projectmxh.Components.OptionItem;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CreatePostActivity extends AppCompatActivity {
    private static final int RESULT_POST_CREATED = 101;

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
    }

    private void initializeViews() {
        postContentEditText = findViewById(R.id.postContentEditText);
        selectedImageView = findViewById(R.id.selectedImageView);
        optionsRecyclerView = findViewById(R.id.optionsRecyclerView);
        bottomSheetView = findViewById(R.id.bottomSheet);
        privacySpinner = findViewById(R.id.privacySpinner);

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
        String content = postContentEditText.getText().toString().trim();
        
        if (content.isEmpty() && selectedImageView.getDrawable() == null) {
            Toast.makeText(this, "Please add some content or image", Toast.LENGTH_SHORT).show();
            return;
        }
        String privacy = privacySpinner.getSelectedItem().toString();

        if (content.isEmpty() && selectedImageView.getDrawable() == null) {
            Toast.makeText(this, "Please add some content or image", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement post creation logic
        Toast.makeText(this, "Creating post...", Toast.LENGTH_SHORT).show();
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
//package com.example.projectmxh.screen;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.ImageButton;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import com.example.projectmxh.R;
//import com.example.projectmxh.service.SignalingService;
//import org.json.JSONObject;
//import org.webrtc.*;
//
//public class VideoCallActivity extends AppCompatActivity implements SignalingService.SignalingCallback {
//    private static final String TAG = "VideoCallActivity";
//    private static final int PERMISSION_REQUEST_CODE = 1;
//
//    private SignalingService signalingService;
//    private PeerConnectionFactory peerConnectionFactory;
//    private PeerConnection peerConnection;
//    private VideoTrack localVideoTrack;
//    private VideoTrack remoteVideoTrack;
//    private SurfaceViewRenderer localVideoView;
//    private SurfaceViewRenderer remoteVideoView;
//    private ImageButton endCallButton;
//    private ImageButton toggleMicButton;
//    private ImageButton toggleCameraButton;
//    private boolean isMicEnabled = true;
//    private boolean isCameraEnabled = true;
//    private String targetUser;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_video_call);
//
//        targetUser = getIntent().getStringExtra("targetUser");
//        initializeViews();
//        checkPermissions();
//    }
//
//    private void initializeViews() {
//        localVideoView = findViewById(R.id.local_video_view);
//        remoteVideoView = findViewById(R.id.remote_video_view);
//        endCallButton = findViewById(R.id.end_call_button);
//        toggleMicButton = findViewById(R.id.toggle_mic_button);
//        toggleCameraButton = findViewById(R.id.toggle_camera_button);
//
//        endCallButton.setOnClickListener(v -> endCall());
//        toggleMicButton.setOnClickListener(v -> toggleMic());
//        toggleCameraButton.setOnClickListener(v -> toggleCamera());
//    }
//
//    private void checkPermissions() {
//        String[] permissions = {
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO
//        };
//
//        boolean allPermissionsGranted = true;
//        for (String permission : permissions) {
//            if (ContextCompat.checkSelfPermission(this, permission)
//                    != PackageManager.PERMISSION_GRANTED) {
//                allPermissionsGranted = false;
//                break;
//            }
//        }
//
//        if (!allPermissionsGranted) {
//            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
//        } else {
//            initializeWebRTC();
//        }
//    }
//
//    // ... More implementation details to follow
//}
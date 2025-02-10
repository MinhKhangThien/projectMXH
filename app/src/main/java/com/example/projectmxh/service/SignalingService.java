//package com.example.projectmxh.service;
//
//import android.util.Log;
//import io.socket.client.IO;
//import io.socket.client.Socket;
//import io.socket.emitter.Emitter;
//import org.json.JSONObject;
//import java.net.URISyntaxException;
//
//public class SignalingService {
//    private static final String TAG = "SignalingService";
//    private Socket socket;
//    private final String serverUrl = "http://10.0.2.2:8080";
//    private SignalingCallback callback;
//
//    public interface SignalingCallback {
//        void onOfferReceived(JSONObject data);
//        void onAnswerReceived(JSONObject data);
//        void onIceCandidateReceived(JSONObject data);
//        void onCallRejected(String message);
//    }
//
//    public SignalingService(SignalingCallback callback) {
//        this.callback = callback;
//        try {
//            socket = IO.socket(serverUrl);
//            setupSocketEvents();
//        } catch (URISyntaxException e) {
//            Log.e(TAG, "Socket creation error: " + e.getMessage());
//        }
//    }
//
//    private void setupSocketEvents() {
//        socket.on("offer", args -> {
//            JSONObject data = (JSONObject) args[0];
//            callback.onOfferReceived(data);
//        });
//
//        socket.on("answer", args -> {
//            JSONObject data = (JSONObject) args[0];
//            callback.onAnswerReceived(data);
//        });
//
//        socket.on("ice_candidate", args -> {
//            JSONObject data = (JSONObject) args[0];
//            callback.onIceCandidateReceived(data);
//        });
//
//        socket.on("call_rejected", args -> {
//            String message = (String) args[0];
//            callback.onCallRejected(message);
//        });
//    }
//
//    public void connect() {
//        if (socket != null) {
//            socket.connect();
//        }
//    }
//
//    public void disconnect() {
//        if (socket != null) {
//            socket.disconnect();
//        }
//    }
//
//    public void sendOffer(String targetUser, JSONObject offer) {
//        socket.emit("offer", targetUser, offer);
//    }
//
//    public void sendAnswer(String targetUser, JSONObject answer) {
//        socket.emit("answer", targetUser, answer);
//    }
//
//    public void sendIceCandidate(String targetUser, JSONObject candidate) {
//        socket.emit("ice_candidate", targetUser, candidate);
//    }
//}

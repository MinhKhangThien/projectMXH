package com.example.projectmxh.config;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.projectmxh.Model.Message;
import com.example.projectmxh.screen.ChatActivity;
import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class WebSocketConfig {
    private static final String SOCKET_URL = "ws://10.0.2.2:8080/ws/websocket";
    private WebSocketClient webSocketClient;
    private final ChatActivity activity;
    private boolean isConnected = false;

    public WebSocketConfig(ChatActivity activity) {
        this.activity = activity;
        connectWebSocket();
    }

    private void connectWebSocket() {
        try {
            URI uri = new URI(SOCKET_URL);
            createWebSocketClient(uri);
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e("WebSocket", "Error: " + e.getMessage());
        }
    }

    private void createWebSocketClient(URI uri) {
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                Log.d("WebSocket", "Connected");
                isConnected = true;
                String currentUser = activity.getCurrentUser();
                if (currentUser != null && !currentUser.isEmpty()) {
                    subscribe("/user/" + currentUser + "/private");
                    Log.d("WebSocket", "Subscribed for user: " + currentUser);
                } else {
                    Log.e("WebSocket", "Current user is null or empty");
                    reconnect();
                }
            }

            @Override
            public void onMessage(String message) {
                Log.d("WebSocket", "Received message: " + message);
                try {
                    Message msgObj = new Gson().fromJson(message, Message.class);
                    Log.d("WebSocket", "Parsed message: " + new Gson().toJson(msgObj));
                    activity.runOnUiThread(() -> activity.handleNewMessage(message));
                } catch (Exception e) {
                    Log.e("WebSocket", "Error parsing message: " + e.getMessage());
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d("WebSocket", "Closed: " + reason);
                isConnected = false;
                if (remote) {
                    reconnect();
                }
            }

            @Override
            public void onError(Exception ex) {
                Log.e("WebSocket", "Error: " + ex.getMessage());
                isConnected = false;
                reconnect();
            }
        };
    }

    private void subscribe(String topic) {
        String subscribeFrame = "SUBSCRIBE\n" +
                "destination:" + topic + "\n" +
                "id:sub-0\n\n\0";
        webSocketClient.send(subscribeFrame);
    }

    public void send(String message) {
        if (isConnected) {
            String sendFrame = "SEND\n" +
                    "destination:/app/private-message\n" +
                    "content-type:application/json\n\n" +
                    message + "\0";
            webSocketClient.send(sendFrame);
        } else {
            Log.e("WebSocket", "Not connected");
            reconnect();
        }
    }

    private void reconnect() {
        if (!isConnected) {
            try {
                Thread.sleep(5000);
                connectWebSocket();
            } catch (InterruptedException e) {
                Log.e("WebSocket", "Reconnection interrupted");
            }
        }
    }

    public void close() {
        if (webSocketClient != null && isConnected) {
            webSocketClient.close();
        }
    }
}
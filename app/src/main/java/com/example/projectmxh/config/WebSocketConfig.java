package com.example.projectmxh.config;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;

import com.example.projectmxh.Model.Message;
import com.example.projectmxh.screen.ChatActivity;
import com.example.projectmxh.screen.GroupChatActivity;
import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class WebSocketConfig {
    private static final String TAG = "WebSocketConfig";
    private final String SOCKET_URL = "ws://10.0.2.2:8080/ws/websocket";
    private WebSocketClient webSocketClient;
    private final Activity activity;
    private boolean isConnected = false;

    public WebSocketConfig(ChatActivity activity) {
        this.activity = activity;
        connectWebSocket();
    }

    public WebSocketConfig(GroupChatActivity activity) {
        // Add constructor for group chat
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
                Log.d(TAG, "Connected");
                isConnected = true;
                if (activity instanceof GroupChatActivity) {
                    String groupId = ((GroupChatActivity) activity).getGroupId();
                    subscribe("/chatroom/group/" + groupId);
                } else if (activity instanceof ChatActivity) {
                    String currentUser = ((ChatActivity) activity).getCurrentUser();
                    if (currentUser != null) {
                        subscribe("/user/" + currentUser + "/private");
                    }
                }
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, "Received message: " + message);
                try {
                    Message msgObj = new Gson().fromJson(message, Message.class);
                    if (activity instanceof GroupChatActivity) {
                        ((GroupChatActivity) activity).runOnUiThread(() ->
                                ((GroupChatActivity) activity).handleNewMessage(message));
                    } else if (activity instanceof ChatActivity) {
                        ((ChatActivity) activity).runOnUiThread(() ->
                                ((ChatActivity) activity).handleNewMessage(message));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing message: " + e.getMessage());
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d(TAG, "Connection closed: " + reason);
                isConnected = false;
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, "Error: " + ex.getMessage());
            }
        };
    }

    private void subscribe(String topic) {
        String subscribeFrame = "SUBSCRIBE\n" +
                "destination:" + topic + "\n" +
                "id:sub-0\n\n\0";
        webSocketClient.send(subscribeFrame);
    }

    public void subscribeToGroupChat(String groupId) {
        if (isConnected) {
            subscribe("/chatroom/group/" + groupId);
        }
    }

    public void send(String destination, String message) {
        if (isConnected) {
            String sendFrame = "SEND\n" +
                    "destination:" + destination + "\n" +
                    "content-type:application/json\n\n" +
                    message + "\0";
            webSocketClient.send(sendFrame);
        } else {
            Log.e(TAG, "WebSocket not connected");
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
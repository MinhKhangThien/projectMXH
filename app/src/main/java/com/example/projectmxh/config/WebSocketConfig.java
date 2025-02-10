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
    public interface WebSocketMessageListener {
        void handleNewMessage(String messageJson);
    }

    private static final String TAG = "WebSocketConfig";
    private final String SOCKET_URL = "ws://10.0.2.2:8080/ws/websocket";
    private WebSocketClient webSocketClient;
    private final Activity activity;
    private final WebSocketMessageListener messageListener;
    private boolean isConnected = false;

    public WebSocketConfig(Activity activity) {
        this.activity = activity;
        if (activity instanceof WebSocketMessageListener) {
            this.messageListener = (WebSocketMessageListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement WebSocketMessageListener");
        }
    }

    private void connectWebSocket() {
        try {
            URI uri = new URI(SOCKET_URL);
            createWebSocketClient(uri, null); // Pass null initially
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e("WebSocket", "Error: " + e.getMessage());
        }
    }

    private void createWebSocketClient(URI uri, String currentUser) {
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                Log.d(TAG, "Connected to WebSocket");
                isConnected = true;

                // Send CONNECT frame
                String connectFrame = "CONNECT\n" +
                        "accept-version:1.1,1.0\n" +
                        "heart-beat:10000,10000\n\n\0";
                webSocketClient.send(connectFrame);

                // Subscribe to private messages
                if (currentUser != null) {
                    String topic = "/user/" + currentUser + "/private";
                    subscribe(topic);
                    Log.d(TAG, "Subscribed to: " + topic);
                }
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, "Received WebSocket message: " + message);

                // Parse message to check if it's a CONNECTED response
                if (message.startsWith("CONNECTED")) {
                    Log.d(TAG, "Successfully connected to STOMP");
                    return;
                }

                // Extract the actual message content
                String messageContent = extractMessageContent(message);
                if (messageContent != null && messageListener != null) {
                    activity.runOnUiThread(() -> {
                        messageListener.handleNewMessage(messageContent);
                    });
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

    private void createWebSocketClientForGroup(URI uri, String currentUser, String groupId) {
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                Log.d(TAG, "Connected to WebSocket");
                isConnected = true;

                // Send CONNECT frame with accept-version and heart-beat
                String connectFrame = "CONNECT\n" +
                        "accept-version:1.1,1.0\n" +
                        "heart-beat:10000,10000\n\n\0";
                send(connectFrame);

                // Subscribe to group topic with proper format
                String topic = "/chatroom/group/" + groupId;
                String subscribeFrame = "SUBSCRIBE\n" +
                        "id:sub-0\n" +
                        "destination:" + topic + "\n" +
                        "ack:auto\n\n\0";
                send(subscribeFrame);

                Log.d(TAG, "Subscribed to group chat: " + topic);
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, "Received message: " + message);

                if (message.startsWith("CONNECTED")) {
                    Log.d(TAG, "STOMP connection established");
                    return;
                }

                try {
                    // Extract message content
                    int startIndex = message.indexOf("\n\n");
                    if (startIndex > -1) {
                        String messageContent = message.substring(startIndex + 2)
                                .replace("\0", "");
                        Log.d(TAG, "Parsed message content: " + messageContent);

                        if (messageListener != null) {
                            activity.runOnUiThread(() ->
                                    messageListener.handleNewMessage(messageContent));
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing message", e);
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

    public void connectAndSubscribeToGroup(String currentUser, String groupId) {
        try {
            URI uri = new URI(SOCKET_URL);
            createWebSocketClientForGroup(uri, currentUser, groupId);
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to WebSocket", e);
        }
    }

    public void sendGroupMessage(String destination, String message) {
        if (!isConnected) {
            Log.e(TAG, "WebSocket not connected. Attempting reconnect...");
            reconnect();
            return;
        }

        // Extract groupId from destination
        String groupId = destination.substring(destination.lastIndexOf("/") + 1);

        // IMPORTANT: Change destination format to match server endpoint
        String correctDestination = "/app/group-message/" + groupId;

        String sendFrame = "SEND\n" +
                "destination:" + correctDestination + "\n" +
                "content-type:application/json\n\n" +
                message + "\0";

        Log.d(TAG, "Sending group message to: " + correctDestination);
        Log.d(TAG, "Message content: " + message);
        webSocketClient.send(sendFrame);
    }

    private void sendGroupMessageFrame(String destination, String message) {
        // Remove any potential null characters from the message
        message = message.replace("\0", "");

        String sendFrame = "SEND\n" +
                "destination:" + destination + "\n" +
                "content-type:application/json\n" +
                "content-length:" + message.length() + "\n\n" +
                message + "\0";

        Log.d(TAG, "Sending WebSocket frame: " + sendFrame);
        webSocketClient.send(sendFrame);
    }

    private String extractMessageContent(String frame) {
        try {
            // Skip headers and find the message body
            int messageStart = frame.indexOf("\n\n");
            if (messageStart > -1) {
                String messageBody = frame.substring(messageStart + 2);
                // Remove null terminator if present
                if (messageBody.endsWith("\0")) {
                    messageBody = messageBody.substring(0, messageBody.length() - 1);
                }
                return messageBody;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting message content", e);
        }
        return null;
    }

    private void subscribe(String topic) {
        String subscribeFrame = "SUBSCRIBE\n" +
                "id:sub-0\n" +
                "destination:" + topic + "\n" +
                "ack:auto\n\n\0";
        webSocketClient.send(subscribeFrame);
        Log.d(TAG, "Sent subscription frame for: " + topic);
    }

    public void connectAndSubscribe(String currentUser) {
        try {
            URI uri = new URI(SOCKET_URL);
            createWebSocketClient(uri, currentUser);
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e("WebSocket", "Error: " + e.getMessage());
        }
    }

    private void subscribeToGroupChat(String groupId) {
        String topic = "/chatroom/group/" + groupId;
        String subscribeFrame = "SUBSCRIBE\n" +
                "id:sub-0\n" +
                "destination:" + topic + "\n" +
                "ack:auto\n\n\0";

        Log.d(TAG, "Subscribing to topic: " + topic);
        webSocketClient.send(subscribeFrame);
    }

    public void send(String destination, String message) {
        if (!isConnected) {
            Log.e(TAG, "WebSocket not connected. Attempting reconnect...");
            reconnect();
            return;
        }

        String sendFrame = "SEND\n" +
                "destination:" + destination + "\n" +
                "content-type:application/json\n\n" +
                message + "\0";

        Log.d(TAG, "Sending message: " + sendFrame);
        webSocketClient.send(sendFrame);
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
package com.example.projectmxh.Components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MessageReadReceiver extends BroadcastReceiver {
    private static final String TAG = "MessageReadReceiver";
    private MessageReadCallback callback;

    public interface MessageReadCallback {
        void onMessageRead(String contactUsername);
    }

    public MessageReadReceiver(MessageReadCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("MESSAGES_READ".equals(intent.getAction())) {
            String contactUsername = intent.getStringExtra("contactUsername");
            Log.d(TAG, "Received message read broadcast for: " + contactUsername);
            if (callback != null) {
                callback.onMessageRead(contactUsername);
            }
        }
    }
}
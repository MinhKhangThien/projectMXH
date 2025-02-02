package com.example.projectmxh.service;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder();

        // Chỉ thêm token nếu token không null
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        Request modifiedRequest = builder.build();
        return chain.proceed(modifiedRequest);
    }
}
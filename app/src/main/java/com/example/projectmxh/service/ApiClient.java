package com.example.projectmxh.service;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/api/v1/";
    private static Retrofit retrofitWithToken;
    private static Retrofit retrofitWithoutToken;

    public static Retrofit getClientWithToken(Context context) {
        if (retrofitWithToken == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .build();

            retrofitWithToken = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofitWithToken;
    }

    public static Retrofit getClientWithoutToken() {
        if (retrofitWithoutToken == null) {
            retrofitWithoutToken = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitWithoutToken;
    }
}

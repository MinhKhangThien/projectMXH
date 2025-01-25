package com.example.projectmxh.service;

import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.dto.request.LoginRequest;
import com.example.projectmxh.dto.request.RegisterRequest;
import com.example.projectmxh.dto.response.LoginResponse;
import com.example.projectmxh.models.Post;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    //auth
    @POST("/api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("/api/v1/auth/signup")
    Call<AppUserDto> register(@Body RegisterRequest registerRequest);

    //post
    @GET("posts/{userId}")
    Call<List<Post>> getPosts(@Path("userId") String userId);

    @Multipart
    @POST("post")
    Call<String> createPost(
            @Part("caption") RequestBody caption,
            @Part("postType") RequestBody postType,
            @Part MultipartBody.Part postContentFile,
            @Part MultipartBody.Part thumbnailFile
    );

}

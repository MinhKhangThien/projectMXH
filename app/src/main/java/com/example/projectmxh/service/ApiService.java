package com.example.projectmxh.service;

import com.example.projectmxh.models.Post;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
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

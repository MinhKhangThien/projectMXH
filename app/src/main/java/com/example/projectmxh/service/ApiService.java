package com.example.projectmxh.service;

import com.example.projectmxh.Model.Comment;
import com.example.projectmxh.Model.GroupChat;
import com.example.projectmxh.Model.Message;
import com.example.projectmxh.config.CloudinaryConfig;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.dto.GroupChatDto;
import com.example.projectmxh.dto.NotificationUserResponseDto;
import com.example.projectmxh.dto.PageData;
import com.example.projectmxh.dto.request.CommentRequest;
import com.example.projectmxh.dto.request.CreatePostRequest;
import com.example.projectmxh.dto.request.LoginRequest;
import com.example.projectmxh.dto.request.PendingFollowRequest;
import com.example.projectmxh.dto.request.RegisterRequest;
import com.example.projectmxh.dto.response.CloudinaryResponse;
import com.example.projectmxh.dto.response.LoginResponse;
import com.example.projectmxh.Model.Post;
import com.example.projectmxh.enums.FollowStatus;

import java.util.List;
import java.util.UUID;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    //auth
    @POST("/api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("/api/v1/auth/signup")
    Call<AppUserDto> register(@Body RegisterRequest registerRequest);

    @GET("/api/v1/auth/user")
    Call<AppUserDto> getMe();


    //user
    @GET("/api/v1/user/{userId}")
    Call<AppUserDto> getUserById(@Path("userId") String userId);

    @GET("/api/v1/user/search")
    Call<List<AppUserDto>> searchUsers(@Query("displayName") String displayName);


    //post
    @GET("/api/v1/post/posts/{userId}")
    Call<List<Post>> getUserPosts(@Path("userId") String userId);

    @POST("/api/v1/post")
    Call<String> createPost(@Body CreatePostRequest createPostRequest);

    @GET("/api/v1/timeline")
    Call<List<Post>> getTimeline();

    @GET("/api/v1/post")
    Call<List<Post>> getMyPosts();

    // like
    @POST("/api/v1/post/like/{postId}")
    Call<Void> likePost(@Path("postId") String postId);

    @GET("/api/v1/like/count/{postId}")
    Call<Integer> getLikeCount(@Path("postId") String postId);

    @POST("/api/v1/comment/like/{commentId}")
    Call<Void> likeComment(@Path("commentId") String commentId);

    @GET("/api/v1/post/like/check/{postId}")
    Call<Boolean> checkPostLike(@Path("postId") String postId);


    //comment
    @POST("/api/v1/post/comment")
    Call<ResponseBody> addComment(@Body CommentRequest comment);

    @GET("/api/v1/post/all-comments/{postId}")
    Call<List<Comment>> getComments(@Path("postId") UUID postId);

    @GET("/api/v1/post/all-comment/{postId}")
    Call<Integer> getCommentCount(@Path("postId") String postId);

    // @POST("/api/v1/post/{commentId}/reply")
    // Call<String> addReply(@Path("commentId") UUID commentId, @Body CommentRequest request);

    @GET("/api/v1/like/comment/count/{commentId}")
    Call<Integer> getCommentLikeCount(@Path("commentId") String commentId);

    @POST("/api/v1/post/{commentId}/reply")
    Call<ResponseBody> replyToComment(@Path("commentId") String commentId, @Body CommentRequest request);

    @GET("/api/v1/post/{commentId}/reply")
    Call<List<Comment>> getCommentReplies(@Path("commentId") String commentId);
    
    @GET("/api/v1/post/comment/{commentId}")
    Call<Integer> getReplyCount(@Path("commentId") String commentId);


    //Chat
    @GET("/api/v1/chat/private-messages/{senderName}/{receiverName}")
    Call<List<Message>> getPrivateMessages(
            @Path("senderName") String senderName,
            @Path("receiverName") String receiverName
    );

    @GET("/api/v1/chat/public-messages")
    Call<List<Message>> getPublicMessages();

    // Group chat
    @POST("/api/v1/chat/create-group-chat")
    Call<GroupChatDto> createGroupChat(@Body List<String> userIds);

    @GET("/api/v1/chat/group-chats")
    Call<List<GroupChat>> getGroupChats();

    @GET("/api/v1/chat/group-chat/{groupId}/messages")
    Call<List<Message>> getGroupChatMessages(@Path("groupId") String groupId);

    @GET("/api/v1/chat/group-chat/{groupId}/users")
    Call<List<AppUserDto>> getGroupChatUsers(@Path("groupId") String groupId);

    @POST("/api/v1/chat/add-user-to-group-chat")
    Call<String> addUserToGroupChat(@Query("groupId") String groupId,
                                    @Query("userId") String userId);

    @POST("/api/v1/chat/remove-user-from-group-chat")
    Call<String> removeUserFromGroupChat(@Query("groupId") String groupId,
                                         @Query("userId") String userId);


    // Follow
    @GET("/api/v1/user/follow/{userId}/followings")
    Call<List<PendingFollowRequest>> getFollowings(@Path("userId") String userId);

    @GET("/api/v1/user/follow/check/{userId}")
    Call<FollowStatus> checkFollowStatus(@Path("userId") String userId);

    @POST("/api/v1/user/follow/{userId}")
    Call<Void> followUser(@Path("userId") String userId);

    @DELETE("/api/v1/user/follow/{userId}")
    Call<Void> unfollowUser(@Path("userId") String userId);


    // Notification
    @GET("/api/v1/notification")
    Call<PageData<NotificationUserResponseDto>> getNotifications(
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("isRead") Boolean isRead
    );

    // For Cloudinary upload (if needed)
    @Multipart
    @POST(CloudinaryConfig.UPLOAD_URL)
    Call<CloudinaryResponse> uploadToCloudinary(
        @Part("upload_preset") RequestBody uploadPreset,
        @Part MultipartBody.Part file
    );

}

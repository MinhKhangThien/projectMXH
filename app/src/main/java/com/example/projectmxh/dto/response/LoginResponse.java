package com.example.projectmxh.dto.response;

public class LoginResponse {
    private String token;
    private String refreshtoken;
    private long expireIn;

    public LoginResponse(String token, String refreshtoken, long expireIn) {
        this.token = token;
        this.refreshtoken = refreshtoken;
        this.expireIn = expireIn;
    }

    public String getToken() {
        return token;
    }
}

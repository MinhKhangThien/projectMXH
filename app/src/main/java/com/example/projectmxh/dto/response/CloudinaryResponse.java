package com.example.projectmxh.dto.response;
import com.google.gson.annotations.SerializedName;
public class CloudinaryResponse {
    @SerializedName("secure_url")
    private String url;
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
}

package com.creatorverse.auth.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private Long userId;

    public AuthResponse(String accessToken, String refreshToken, Long userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

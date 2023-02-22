package com.example.lamivhan.utill.dto;

public class DTOtokens {

    private final String AccessToken;

    private final String RefreshToken;

    private final String userEmail;

    public DTOtokens(String accessToken, String refreshToken, String userEmail) {
        AccessToken = accessToken;
        RefreshToken = refreshToken;
        this.userEmail = userEmail;
    }

    public String getAccessToken() {
        return AccessToken;
    }

    public String getRefreshToken() {
        return RefreshToken;
    }

    public String getUserEmail() {
        return userEmail;
    }
}

package com.example.planit.utill.dto;

public class DTOtokens {

    private final String AccessToken;

    private final long expireTimeInMilliseconds;

    private final String RefreshToken;

    private final String userEmail;

    public DTOtokens(String accessToken, long expireTimeInMilliseconds, String refreshToken, String userEmail) {
        AccessToken = accessToken;
        RefreshToken = refreshToken;
        this.userEmail = userEmail;
        this.expireTimeInMilliseconds = expireTimeInMilliseconds;
    }

    public String getAccessToken() {
        return AccessToken;
    }

    public String getRefreshToken() {
        return RefreshToken;
    }

    public long getExpireTimeInMilliseconds() {
        return expireTimeInMilliseconds;
    }

    public String getUserEmail() {
        return userEmail;
    }
}

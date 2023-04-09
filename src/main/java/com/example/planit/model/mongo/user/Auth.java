package com.example.planit.model.mongo.user;

import org.springframework.data.mongodb.core.mapping.Field;

public class Auth {

    @Field(name = "access_token")
    private String accessToken;

    @Field(name = "expires_in")
    private long expireTimeInMilliseconds;

    @Field(name = "refresh_token")
    private String refreshToken;

    public Auth(String accessToken, long expireTimeInMilliseconds, String refreshToken) {
        this.accessToken = accessToken;
        this.expireTimeInMilliseconds = expireTimeInMilliseconds;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpireTimeInMilliseconds() {
        return expireTimeInMilliseconds;
    }

    public void setExpireTimeInMilliseconds(long expireTimeInMilliseconds) {
        this.expireTimeInMilliseconds = expireTimeInMilliseconds;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

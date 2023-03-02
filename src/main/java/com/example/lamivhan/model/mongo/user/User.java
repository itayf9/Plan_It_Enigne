package com.example.lamivhan.model.mongo.user;

import com.example.lamivhan.model.preferences.Preferences;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("users")
public class User {

    @Id
    private String id;

    @Field(name = "email")
    private String email;

    @Field(name = "user_preferences")
    private Preferences userPreferences;

    @Field(name = "planIt_calendar_ID")
    private String planItCalendarID;

    @Field(name = "access_token")
    private String accessToken;

    @Field(name = "expires_in")
    private long expireTimeInMilliseconds;

    @Field(name = "refresh_token")
    private String refreshToken;

    // need empty ctor for mongo reflection stuff
    public User() {
    }

    public User(String email, String accessToken, long expireTimeInMilliseconds, String refreshToken) {
        this.userPreferences = new Preferences();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.expireTimeInMilliseconds = expireTimeInMilliseconds;
        this.planItCalendarID = null;
    }

    public String getEmail() {
        return email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Preferences getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(Preferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    public String getPlanItCalendarID() {
        return planItCalendarID;
    }

    public void setPlanItCalendarID(String planItCalendarID) {
        this.planItCalendarID = planItCalendarID;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpireTimeInMilliseconds() {
        return expireTimeInMilliseconds;
    }

    public void setExpireTimeInMilliseconds(long expireTimeInMilliseconds) {
        this.expireTimeInMilliseconds = expireTimeInMilliseconds;
    }
}

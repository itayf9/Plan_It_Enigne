package com.example.planit.model.mongo.user;

import com.example.planit.model.preferences.Preferences;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("users")
public class User {

    @Id
    private String subjectID;

    @Field(name = "email")
    private String email;

    @Field(name = "name")
    private String name;

    @Field(name = "picture_url")
    private String pictureUrl;

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

    public User(String subjectId, String email, String name, String pictureUrl, String accessToken, long expireTimeInMilliseconds, String refreshToken) {
        this.subjectID = subjectId;
        this.email = email;
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.userPreferences = new Preferences();
        this.planItCalendarID = null;
        this.accessToken = accessToken;
        this.expireTimeInMilliseconds = expireTimeInMilliseconds;
        this.refreshToken = refreshToken;

    }

    public String getSubjectId() {
        return subjectID;
    }

    // user cant change is subject id, remove this
    public void setSubjectId(String subjectId) {
        this.subjectID = subjectId;
    }

    public String getEmail() {
        return email;
    }

    // user cant change is email, remove this
    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    // user cant change is name (in our app at least), remove this
    public void setName(String name) {
        this.name = name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    // we won't change google stuff, remove this
    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public Preferences getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(Preferences userPreferences) {
        this.userPreferences = userPreferences;
    }


    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
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

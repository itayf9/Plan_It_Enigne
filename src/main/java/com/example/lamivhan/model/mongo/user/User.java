package com.example.lamivhan.model.mongo.user;

import com.example.lamivhan.model.preferences.Preferences;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("users")
public class User {

    @Id
    private String id;

    @Field(name = "user_name")
    private String userName;
    @Field(name = "is_day_learner")
    private boolean isDayLearner;

    @Field(name = "email")
    private final String email;

    @Field(name = "user_preferences")
    private Preferences userPreferences;

    @Field(name = "planIt_calendar_ID")
    private String planItCalendarID;

    @Field(name = "access_token")
    private String accessToken;

    @Field(name = "refresh_token")
    private String refreshToken;

    public User(String userName, boolean isDayLearner, Preferences userPreferences, String email) {
        this.userName = userName;
        this.isDayLearner = isDayLearner;
        this.userPreferences = userPreferences;
        this.email = email;
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

    public User(String email, String accessToken, String refreshToken) {
        this.userPreferences = new Preferences();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isDayLearner() {
        return isDayLearner;
    }

    public void setDayLearner(boolean dayLearner) {
        isDayLearner = dayLearner;
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
}

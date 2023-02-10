package com.example.lamivhan.model.user;

import com.example.lamivhan.model.preferences.Preferences;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
@Document("users")
public class User {
    @Field(name = "user_name")
    private String userName;
    @Field(name = "is_day_learner")
    private boolean isDayLearner;

    @Field(name = "user_preferences")
    private Preferences userPreferences;

    public User(String userName, boolean isDayLearner, Preferences userPreferences) {
        this.userName = userName;
        this.isDayLearner = isDayLearner;
        this.userPreferences = userPreferences;
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
}

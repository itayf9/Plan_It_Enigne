package com.example.planit.model.mongo.user;

import com.example.planit.model.mongo.user.preferences.Preferences;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("users")
public class User {

    @Id
    private String subjectID;

    @Field(name = "profile")
    private Profile profile;

    @Field(name = "user_preferences")
    private Preferences userPreferences;

    @Field(name = "planIt_calendar_ID")
    private String planItCalendarID;

    @Field(name = "auth")
    private Auth auth;

    @Field(name = "latest_study_plan")
    private StudyPlan latestStudyPlan;

    @Field(name = "is_admin")
    private boolean isAdmin;

    @Field(name = "is_completed_first_setup")
    private boolean isCompletedFirstSetup;

    public boolean isCompletedFirstSetup() {
        return isCompletedFirstSetup;
    }

    public void setCompletedFirstSetup(boolean completedFirstSetup) {
        isCompletedFirstSetup = completedFirstSetup;
    }

    // need empty ctor for mongo reflection stuff
    public User() {
    }

    public User(String subjectId, String email, String name, String pictureUrl, String accessToken, long expireTimeInMilliseconds, String refreshToken) {
        this.subjectID = subjectId;
        this.profile = new Profile(email, name, pictureUrl);
        this.userPreferences = new Preferences();
        this.planItCalendarID = null;
        this.auth = new Auth(accessToken, expireTimeInMilliseconds, refreshToken);
        this.latestStudyPlan = null;
        this.isAdmin = false;
        this.isCompletedFirstSetup = false;
    }

    public String getSubjectId() {
        return subjectID;
    }

    public Profile getProfile() {
        return profile;
    }

    public Auth getAuth() {
        return auth;
    }

    public StudyPlan getLatestStudyPlan() {
        return latestStudyPlan;
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

    public void setLatestStudyPlan(StudyPlan studyPlan) {
        this.latestStudyPlan = studyPlan;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}

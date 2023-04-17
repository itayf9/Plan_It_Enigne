package com.example.planit.model.mongo.user;

import com.example.planit.model.mongo.user.preferences.Preferences;


public class UserClientRepresentation {

    private String subjectID;

    private Profile profile;

    private Preferences userPreferences;

    private String planItCalendarID;

    private StudyPlan latestStudyPlan;

    private boolean isAdmin;

    private UserClientRepresentation(String subjectID, Profile profile, Preferences userPreferences, String planItCalendarID, StudyPlan latestStudyPlan, boolean isAdmin) {
        this.subjectID = subjectID;
        this.profile = profile;
        this.userPreferences = userPreferences;
        this.planItCalendarID = planItCalendarID;
        this.latestStudyPlan = latestStudyPlan;
        this.isAdmin = isAdmin;
    }

    public static UserClientRepresentation buildUserClientRepresentationFromUser(User userToBeConverted) {
        return new UserClientRepresentation(userToBeConverted.getSubjectId(),
                userToBeConverted.getProfile(),
                userToBeConverted.getUserPreferences(),
                userToBeConverted.getPlanItCalendarID(),
                userToBeConverted.getLatestStudyPlan(),
                userToBeConverted.isAdmin());
    }

    public String getSubjectID() {
        return subjectID;
    }

    public Profile getProfile() {
        return profile;
    }

    public Preferences getUserPreferences() {
        return userPreferences;
    }

    public String getPlanItCalendarID() {
        return planItCalendarID;
    }

    public StudyPlan getLatestStudyPlan() {
        return latestStudyPlan;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}

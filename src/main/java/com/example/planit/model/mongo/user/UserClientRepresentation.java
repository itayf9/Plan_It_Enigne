package com.example.planit.model.mongo.user;

import com.example.planit.model.mongo.user.preferences.Preferences;


public class UserClientRepresentation {

    private final String subjectID;

    private final Profile profile;

    private final Preferences userPreferences;

    private final String planItCalendarID;

    private final StudyPlan latestStudyPlan;

    private final boolean isAdmin;

    private final boolean isCompletedFirstSetup;


    private UserClientRepresentation(String subjectID, Profile profile, Preferences userPreferences, String planItCalendarID, StudyPlan latestStudyPlan, boolean isAdmin, boolean isCompletedFirstSetup) {
        this.subjectID = subjectID;
        this.profile = profile;
        this.userPreferences = userPreferences;
        this.planItCalendarID = planItCalendarID;
        this.latestStudyPlan = latestStudyPlan;
        this.isAdmin = isAdmin;
        this.isCompletedFirstSetup = isCompletedFirstSetup;
    }

    public static UserClientRepresentation buildUserClientRepresentationFromUser(User userToBeConverted) {
        return new UserClientRepresentation(userToBeConverted.getSubjectId(),
                userToBeConverted.getProfile(),
                userToBeConverted.getUserPreferences(),
                userToBeConverted.getPlanItCalendarID(),
                userToBeConverted.getLatestStudyPlan(),
                userToBeConverted.isAdmin(),
                userToBeConverted.isCompletedFirstSetup());
    }

    public String getSubjectID() {
        return subjectID;
    }

    public boolean isCompletedFirstSetup() {
        return isCompletedFirstSetup;
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

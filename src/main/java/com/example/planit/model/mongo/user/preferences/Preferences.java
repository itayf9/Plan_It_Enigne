package com.example.planit.model.mongo.user.preferences;

import static com.example.planit.utill.defaults.Defaults.*;

public class Preferences {

    private int userStudyStartTime; // 0800 will be converted to 8 to represent 08:00 in the morning
    private int userStudyEndTime; // 2200 will be converted to 22 to represent 22:00 at night
    private int userBreakTime; // in minutes e.g. 15 minutes
    private int studySessionTime; // in minutes e.g. 120 minutes ~ 2 hours
    private boolean isStudyOnWeekends;

    public Preferences() {
        this.studySessionTime = DEFAULT_USER_STUDY_SESSION_TIME;
        this.isStudyOnWeekends = DEFAULT_USER_IS_STUDY_ON_WEEKENDS;
        this.userBreakTime = DEFAULT_USER_BREAK_TIME;
        this.userStudyStartTime = DEFAULT_USER_STUDY_START_TIME;
        this.userStudyEndTime = DEFAULT_USER_STUDY_END_TIME;
    }

    /*@JsonCreator
    public Preferences(@JsonProperty("userStudyStartTime") int userStudyStartTime,
                       @JsonProperty("userStudyEndTime") int userStudyEndTime,
                       @JsonProperty("userBreakTime") int userBreakTime,
                       @JsonProperty("studySessionTime") int studySessionTime,
                       @JsonProperty("studyOnWeekends") boolean isStudyOnWeekends) {
        this.userStudyStartTime = userStudyStartTime;
        this.userStudyEndTime = userStudyEndTime;
        this.userBreakTime = userBreakTime;
        this.studySessionTime = studySessionTime;
        this.isStudyOnWeekends = isStudyOnWeekends;
    }*/

    public int getUserStudyStartTime() {
        return userStudyStartTime;
    }

    public int getUserStudyEndTime() {
        return userStudyEndTime;
    }

    public int getUserBreakTime() {
        return userBreakTime;
    }

    public int getStudySessionTime() {
        return studySessionTime;
    }

    public boolean isStudyOnWeekends() {
        return isStudyOnWeekends;
    }

    public void setUserStudyStartTime(int userStudyStartTime) {
        this.userStudyStartTime = userStudyStartTime;
    }

    public void setUserStudyEndTime(int userStudyEndTime) {
        this.userStudyEndTime = userStudyEndTime;
    }

    public void setUserBreakTime(int userBreakTime) {
        this.userBreakTime = userBreakTime;
    }

    public void setStudySessionTime(int studySessionTime) {
        this.studySessionTime = studySessionTime;
    }

    public void setStudyOnWeekends(boolean studyOnWeekends) {
        isStudyOnWeekends = studyOnWeekends;
    }
}

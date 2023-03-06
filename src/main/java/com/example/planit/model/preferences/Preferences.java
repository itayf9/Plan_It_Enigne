package com.example.planit.model.preferences;

import static com.example.planit.utill.defaults.Defaults.*;

public class Preferences {

    private int userStudyStartTime;
    private int userStudyEndTime;
    private int userBreakTime; // in minutes e.g. 15 minutes
    private int studySessionTime;
    private boolean isStudyOnHolyDays;

    public Preferences() {
        this.studySessionTime = DEFAULT_USER_STUDY_SESSION_TIME;
        this.isStudyOnHolyDays = false;
        this.userBreakTime = DEFAULT_USER_BREAK_TIME;
        this.userStudyStartTime = DEFAULT_USER_STUDY_START_TIME;
        this.userStudyEndTime = DEFAULT_USER_STUDY_END_TIME;
    }

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

    public boolean isStudyOnHolyDays() {
        return isStudyOnHolyDays;
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

    public void setStudyOnHolyDays(boolean studyOnHolyDays) {
        isStudyOnHolyDays = studyOnHolyDays;
    }
}

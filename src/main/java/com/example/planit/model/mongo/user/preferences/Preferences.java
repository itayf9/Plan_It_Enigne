package com.example.planit.model.mongo.user.preferences;

import static com.example.planit.utill.defaults.Defaults.*;

public class Preferences {

    private final int userStudyStartTime; // 0800 will be converted to 8 to represent 08:00 in the morning
    private final int userStudyEndTime; // 2200 will be converted to 22 to represent 22:00 at night
    private final int userBreakTime; // in minutes e.g. 15 minutes
    private final int studySessionTime; // in minutes e.g. 120 minutes ~ 2 hours
    private final boolean isStudyOnHolidays;
    private final boolean isStudyOnWeekends;

    public Preferences() {
        this.studySessionTime = DEFAULT_USER_STUDY_SESSION_TIME;
        this.isStudyOnHolidays = DEFAULT_USER_IS_STUDY_ON_HOLIDAYS;
        this.isStudyOnWeekends = DEFAULT_USER_IS_STUDY_ON_WEEKENDS;
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

    public boolean isStudyOnHolidays() {
        return isStudyOnHolidays;
    }

    public void setUserStudyStartTime(int userStudyStartTime) {
        this.userStudyStartTime = userStudyStartTime;
    }

    public boolean isStudyOnWeekends() {
        return isStudyOnWeekends;
    }

}

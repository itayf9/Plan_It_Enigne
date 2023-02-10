package com.example.lamivhan.model.preferences;

public class Preferences {

    private int userStudyStartTime;
    private int userStudyEndTime;
    private int userBreakTime;
    private int studySessionTime;

    private boolean isStudyOnHolyDays;

    public Preferences() {
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

package com.example.lamivhan.model.studysession;

import com.example.lamivhan.model.timeslot.TimeSlot;
import com.google.api.client.util.DateTime;

public class StudySession extends TimeSlot {

    private String courseName;

    public StudySession(DateTime start, DateTime end) {
        super(start, end);
        this.courseName = "";
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}

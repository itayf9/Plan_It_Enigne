package com.example.lamivhan.model.studysession;

import com.example.lamivhan.model.timeslot.TimeSlot;
import com.google.api.client.util.DateTime;

public class StudySession extends TimeSlot {

    private String courseName;
    private String description;

    public StudySession(DateTime start, DateTime end) {
        super(start, end);
        this.courseName = "";
        this.description = "";
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

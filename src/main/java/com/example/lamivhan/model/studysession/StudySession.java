package com.example.lamivhan.model.studysession;

import com.example.lamivhan.model.timeslot.TimeSlot;
import com.google.api.client.util.DateTime;

public class StudySession extends TimeSlot {
    // add private String summery

    public StudySession(DateTime start, DateTime end) {
        super(start, end);
    }
}

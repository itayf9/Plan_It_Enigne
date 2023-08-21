package com.example.planit.model.event;

import com.example.planit.model.timeslot.TimeSlot;
import com.google.api.client.util.DateTime;

public class EventClientRepresentation extends TimeSlot {
    private final String courseName;
    private final String description;

    public EventClientRepresentation(DateTime start, DateTime end, String courseName, String description) {
        super(start, end);
        this.courseName = courseName;
        this.description = description;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDescription() {
        return description;
    }
}

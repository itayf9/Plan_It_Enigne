package com.example.planit.utill.dto;

import com.example.planit.model.mongo.user.StudyPlan;
import com.google.api.services.calendar.model.Event;

import java.util.List;

public class DTOscanResponseToClient extends DTOstatus {
    private final List<Event> fullDayEvents;
    private final StudyPlan studyPlan;

    public DTOscanResponseToClient(boolean isSucceed, String details, List<Event> fullDayEvents, StudyPlan studyPlan) {
        super(isSucceed, details);
        this.fullDayEvents = fullDayEvents;
        this.studyPlan = studyPlan;
    }

    public StudyPlan getStudyPlan() {
        return studyPlan;
    }

    public List<Event> getFullDayEvents() {
        return fullDayEvents;
    }
}

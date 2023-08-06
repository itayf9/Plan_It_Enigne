package com.example.planit.utill.dto;

import com.example.planit.model.mongo.user.StudyPlan;
import com.example.planit.model.studysession.StudySession;
import com.google.api.services.calendar.model.Event;

import java.util.List;

public class DTOscanResponseToClient extends DTOstatus {
    private final List<Event> fullDayEvents;
    private final StudyPlan studyPlan;

    private final StudySession upComingSession;


    public DTOscanResponseToClient(boolean isSucceed, String details, List<Event> fullDayEvents, StudyPlan studyPlan, StudySession upComingSession) {
        super(isSucceed, details);
        this.fullDayEvents = fullDayEvents;
        this.studyPlan = studyPlan;
        this.upComingSession = upComingSession;
    }

    public StudyPlan getStudyPlan() {
        return studyPlan;
    }

    public List<Event> getFullDayEvents() {
        return fullDayEvents;
    }

    public StudySession getUpComingSession() {
        return upComingSession;
    }
}

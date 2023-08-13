package com.example.planit.utill.dto;

import com.example.planit.model.mongo.user.StudyPlan;
import com.example.planit.model.studysession.StudySession;
import com.google.api.services.calendar.model.Event;
import org.springframework.http.HttpStatus;

import java.util.List;

public class DTOscanResponseToController extends DTOresponseToController {
    private final List<Event> fullDayEvents;

    private final StudyPlan studyPlan;

    private final StudySession upComingSession;


    public DTOscanResponseToController(boolean isSucceed, String details, HttpStatus httpStatus) {
        super(isSucceed, details, httpStatus);
        this.fullDayEvents = null;
        this.studyPlan = null;
        this.upComingSession = null;
    }

    public DTOscanResponseToController(boolean isSucceed, String details, HttpStatus httpStatus, List<Event> fullDayEvents) {
        super(isSucceed, details, httpStatus);
        this.fullDayEvents = fullDayEvents;
        this.studyPlan = null;
        this.upComingSession = null;
    }

    public DTOscanResponseToController(boolean isSucceed, String details, HttpStatus httpStatus, StudyPlan studyPlan, StudySession upComingSession) {
        super(isSucceed, details, httpStatus);
        this.fullDayEvents = null;
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

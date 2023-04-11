package com.example.planit.utill.dto;

import com.example.planit.model.mongo.user.StudyPlan;
import com.google.api.services.calendar.model.Event;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class DTOscanResponseToController extends DTOstatus {
    private final HttpStatus httpStatus;
    private final List<Event> fullDayEvents;

    private final StudyPlan studyPlan;

    public DTOscanResponseToController(boolean isSucceed, String details, HttpStatus httpStatus, List<Event> fullDayEvents, StudyPlan studyPlan) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
        this.fullDayEvents = fullDayEvents;
        this.studyPlan = studyPlan;
    }

    public DTOscanResponseToController(boolean isSucceed, String details, HttpStatus httpStatus) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
        this.fullDayEvents = new ArrayList<>();
        this.studyPlan = new StudyPlan();
    }

    public DTOscanResponseToController(boolean isSucceed, String details, HttpStatus httpStatus, List<Event> fullDayEvents) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
        this.fullDayEvents = fullDayEvents;
        this.studyPlan = new StudyPlan();
    }

    public DTOscanResponseToController(boolean isSucceed, String details, HttpStatus httpStatus, StudyPlan studyPlan) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
        this.fullDayEvents = new ArrayList<>();
        this.studyPlan = studyPlan;
    }

    public StudyPlan getStudyPlan() {
        return studyPlan;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public List<Event> getFullDayEvents() {
        return fullDayEvents;
    }
}

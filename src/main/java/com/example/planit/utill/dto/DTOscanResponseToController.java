package com.example.planit.utill.dto;

import com.google.api.services.calendar.model.Event;
import org.springframework.http.HttpStatus;

import java.util.List;

public class DTOscanResponseToController extends DTOstatus {
    private HttpStatus httpStatus;
    private List<Event> fullDayEvents;

    public DTOscanResponseToController(boolean isSucceed, String details, HttpStatus httpStatus, List<Event> fullDayEvents) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
        this.fullDayEvents = fullDayEvents;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public List<Event> getFullDayEvents() {
        return fullDayEvents;
    }
}

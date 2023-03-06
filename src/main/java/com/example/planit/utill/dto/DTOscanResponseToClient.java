package com.example.planit.utill.dto;

import com.google.api.services.calendar.model.Event;

import java.util.List;

public class DTOscanResponseToClient extends DTOstatus {
    private List<Event> fullDayEvents;

    public DTOscanResponseToClient(boolean isSucceed, String details, List<Event> fullDayEvents) {
        super(isSucceed, details);
        this.fullDayEvents = fullDayEvents;
    }

    public List<Event> getFullDayEvents() {
        return fullDayEvents;
    }
}

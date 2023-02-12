package com.example.lamivhan.utill.dto;

import com.example.lamivhan.model.exam.Exam;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.util.List;

/**
 * DTO that holds of the events the user have + the user exams that were found + full day events + Calendar Service.
 * is using to help us in scan, generate function.
 */
public class DTOuserEvents {
    private final List<Event> fullDayEvents;
    private final List<Exam> examsFound;
    private final List<Event> events;
    private final Calendar calendarService;

    public DTOuserEvents(List<Event> fullDayEvents, List<Exam> examsFound, List<Event> events, Calendar calendarService) {
        this.fullDayEvents = fullDayEvents;
        this.examsFound = examsFound;
        this.events = events;
        this.calendarService = calendarService;
    }

    public List<Event> getFullDayEvents() {
        return fullDayEvents;
    }

    public List<Exam> getExamsFound() {
        return examsFound;
    }

    public List<Event> getEvents() {
        return events;
    }

    public Calendar getCalendarService() {
        return calendarService;
    }
}

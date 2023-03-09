package com.example.planit.utill.dto;

import com.example.planit.model.exam.Exam;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.util.List;

/**
 * DTO that holds of the events the user have + the user exams that were found + full day events + Calendar Service.
 * is using to help us in scan, generate function.
 */
public class DTOuserCalendarsInformation {
    private final List<Event> fullDayEvents;

    private final List<Event> planItCalendarOldEvents;
    private final List<Exam> examsFound;
    private final List<Event> events;
    private final Calendar calendarService;

    public DTOuserCalendarsInformation(List<Event> fullDayEvents, List<Event> planItCalendarOldEvents, List<Exam> examsFound, List<Event> events, Calendar calendarService) {
        this.fullDayEvents = fullDayEvents;
        this.planItCalendarOldEvents = planItCalendarOldEvents;
        this.examsFound = examsFound;
        this.events = events;
        this.calendarService = calendarService;
    }

    public List<Event> getFullDayEvents() {
        return fullDayEvents;
    }

    public List<Event> getPlanItCalendarOldEvents() {
        return planItCalendarOldEvents;
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

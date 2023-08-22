package com.example.planit.model.event;

import com.google.api.services.calendar.model.Event;

import java.util.Comparator;

public class EventComparator implements Comparator<Event> {

    @Override
    public int compare(Event o1, Event o2) {
        boolean o1IsFullDayEvent = o1.getStart().getDate() != null;
        boolean o2IsFullDayEvent = o2.getStart().getDate() != null;

        // Case when both are full day events
        if (o1IsFullDayEvent && o2IsFullDayEvent) {
            return Long.compare(o1.getStart().getDate().getValue(), o2.getStart().getDate().getValue());
        }
        // Case when o1 is a full day event
        else if (o1IsFullDayEvent) {
            return Long.compare(o1.getStart().getDate().getValue(), o2.getStart().getDateTime().getValue());
        }
        // Case when o2 is a full day event
        else if (o2IsFullDayEvent) {
            return Long.compare(o1.getStart().getDateTime().getValue(), o2.getStart().getDate().getValue());
        }
        // Case when both are regular events
        else {
            return Long.compare(o1.getStart().getDateTime().getValue(), o2.getStart().getDateTime().getValue());
        }
    }
}

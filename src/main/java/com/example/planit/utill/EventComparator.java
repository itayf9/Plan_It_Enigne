package com.example.planit.utill;

import com.google.api.services.calendar.model.Event;

import java.util.Comparator;

public class EventComparator implements Comparator<Event> {

    @Override
    public int compare(Event o1, Event o2) {
        if (o1.getStart().getDate() != null || o2.getStart().getDate() != null) {
            return 0;
        }
        return (int) ((o1.getStart().getDateTime().getValue()) - (o2.getStart().getDateTime().getValue()));
    }
}

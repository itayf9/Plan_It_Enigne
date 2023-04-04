package com.example.planit.utill;

import com.google.api.services.calendar.model.Event;

import java.util.Comparator;

public class EventComparator implements Comparator<Event> {

    @Override
    public int compare(Event o1, Event o2) {
        // case of o1 && o2 are full day events
        if (o1.getStart().getDate() != null && o2.getStart().getDate() != null) {
            return (int) ((o1.getStart().getDate().getValue()) - (o2.getStart().getDate().getValue()));
        }
        // case of o1 is a full day events
        if (o1.getStart().getDate() != null) {
            return (int) ((o1.getStart().getDate().getValue()) - (o2.getStart().getDateTime().getValue()));
        }
        // case of o2 is a full day events
        if (o2.getStart().getDate() != null) {
            return (int) ((o1.getStart().getDateTime().getValue()) - (o2.getStart().getDate().getValue()));
        }
        // case of regular events
        return (int) ((o1.getStart().getDateTime().getValue()) - (o2.getStart().getDateTime().getValue()));
    }
}

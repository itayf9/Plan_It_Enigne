package com.example.planit.engine.calendar.event;

import com.example.planit.model.event.EventComparator;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventComparatorTest {

    @Test
    public void testEventComparator() {
        EventComparator comparator = new EventComparator();
        // Create some sample events for testing
        long dateTime1 = 1690804800000L;
        long dateTime2 = 1690884000000L;
        long dateTime3 = 1690963200000L;
        long fullDayDate1 = 1691804800000L;
        long fullDayDate2 = 1692804800000L;

        Event event1 = new Event().setStart(new EventDateTime().setDateTime(new DateTime(dateTime1)));
        Event event2 = new Event().setStart(new EventDateTime().setDateTime(new DateTime(dateTime2)));
        Event event3 = new Event().setStart(new EventDateTime().setDateTime(new DateTime(dateTime3)));
        Event eventFullDay1 = new Event().setStart(new EventDateTime().setDate(new DateTime(fullDayDate1)));
        Event eventFullDay2 = new Event().setStart(new EventDateTime().setDate(new DateTime(fullDayDate2)));

        List<Event> events = new ArrayList<>();
        events.add(event2);
        events.add(event3);
        events.add(event1);
        events.add(eventFullDay1);
        events.add(eventFullDay2);

        // Shuffle the list to ensure the comparator handles unordered events correctly
        Collections.shuffle(events);

        // Sort the events using the comparator
        events.sort(comparator);

        // The sorted order should be: event1, event2, event3, eventFullDay1, eventFullDay2
        assertEquals(event1, events.get(0));
        assertEquals(event2, events.get(1));
        assertEquals(event3, events.get(2));
        assertEquals(eventFullDay1, events.get(3));
        assertEquals(eventFullDay2, events.get(4));
    }

    @Test
    public void testEventComparator2() {
        EventComparator comparator = new EventComparator();

        // Create some sample events for testing
        long fullDayDate1 = 1687359469000L;
        long fullDayDate2 = 1688601600000L;
        long fullDayDate3 = 1690329600000L;
        long fullDayDate4 = 1690329600000L;
        long fullDayDate5 = 1690416000000L;

        Event eventFullDay1 = new Event().setStart(new EventDateTime().setDate(new DateTime(fullDayDate1)));
        Event eventFullDay2 = new Event().setStart(new EventDateTime().setDate(new DateTime(fullDayDate2)));
        Event eventFullDay3 = new Event().setStart(new EventDateTime().setDate(new DateTime(fullDayDate3)));
        Event eventFullDay4 = new Event().setStart(new EventDateTime().setDate(new DateTime(fullDayDate4)));
        Event eventFullDay5 = new Event().setStart(new EventDateTime().setDate(new DateTime(fullDayDate5)));

        List<Event> events = new ArrayList<>();
        events.add(eventFullDay3);
        events.add(eventFullDay4);
        events.add(eventFullDay5);
        events.add(eventFullDay1);
        events.add(eventFullDay2);

        // Shuffle the list to ensure the comparator handles unordered events correctly
        Collections.shuffle(events);

        // Sort the events using the comparator
        events.sort(comparator);

        // The sorted order should be: event1, event2, event3, eventFullDay1, eventFullDay2
        assertEquals(eventFullDay1, events.get(0));
        assertEquals(eventFullDay2, events.get(1));
        assertEquals(eventFullDay3, events.get(2));
        assertEquals(eventFullDay4, events.get(3));
        assertEquals(eventFullDay5, events.get(4));
    }
}

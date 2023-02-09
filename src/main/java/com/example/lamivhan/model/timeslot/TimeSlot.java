package com.example.lamivhan.model.timeslot;

import com.google.api.client.util.DateTime;

public class TimeSlot {

    private DateTime start;
    private DateTime end;

    public TimeSlot(DateTime start, DateTime end) {
        this.start = start;
        this.end = end;
    }

    /**
     * return difference between endTime & start time
     */
    public int getDiffTime() {
        return (int) ((end.getValue() - start.getValue()) / 3600);
    }

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }
}

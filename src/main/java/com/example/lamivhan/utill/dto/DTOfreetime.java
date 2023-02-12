package com.example.lamivhan.utill.dto;

import com.example.lamivhan.model.timeslot.TimeSlot;

import java.util.List;

/**
 * DTO that holds the List of Slots that represents the free time that user has to study until the exams + the user total free time in hours
 */

public class DTOfreetime {
    private final List<TimeSlot> freeTimeSlots;
    private final int TotalFreeTime;

    public DTOfreetime(List<TimeSlot> freeTimeSlots, int totalFreeTime) {
        this.freeTimeSlots = freeTimeSlots;
        TotalFreeTime = totalFreeTime;
    }

    public List<TimeSlot> getFreeTimeSlots() {
        return freeTimeSlots;
    }

    public int getTotalFreeTime() {
        return TotalFreeTime;
    }
}

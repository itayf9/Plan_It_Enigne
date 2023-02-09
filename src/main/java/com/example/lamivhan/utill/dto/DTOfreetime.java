package com.example.lamivhan.utill.dto;

import com.example.lamivhan.model.timeslot.TimeSlot;

import java.util.List;

public class DTOfreetime {
    private List<TimeSlot> freeTimeSlots;
    private int TotalFreeTime;

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

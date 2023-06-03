package com.example.planit.holidays;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PlanITHolidays {
    private Set<String> holidays;

    public Set<String> getHolidays() {
        return holidays;
    }

    public void setHolidays(Set<String> holidays) {
        this.holidays = holidays;
    }
}

package com.example.planit.holidays;

import org.springframework.stereotype.Service;
import com.example.planit.model.mongo.holiday.Holiday;
import java.util.List;

@Service
public class PlanITHolidays {
    private List<Holiday> holidays;

    public List<Holiday> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<Holiday> holidays) {
        this.holidays = holidays;
    }
}

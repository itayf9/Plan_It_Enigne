package com.example.planit.model.mongo.holiday;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("holidays")

public class Holiday {
    @Field(name = "name")
    private String holidayName;
    @Field(name = "date")
    private String holidayStartDate;

    public Holiday() {
    }

    public Holiday(String holidayName, String holidayStartDate) {
        this.holidayName = holidayName;
        this.holidayStartDate = holidayStartDate;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public String getHolidayStartDate() {
        return holidayStartDate;
    }

    public void setHolidayStartDate(String holidayStartDate) {
        this.holidayStartDate = holidayStartDate;
    }
}

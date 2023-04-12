package com.example.planit.model.exam;

public class ExamClientRepresentation {
    private final String courseName;

    private final String dateTimeISO;

    public ExamClientRepresentation(String courseName, String dateTimeISO) {
        this.courseName = courseName;
        this.dateTimeISO = dateTimeISO;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDateTimeISO() {
        return dateTimeISO;
    }
}

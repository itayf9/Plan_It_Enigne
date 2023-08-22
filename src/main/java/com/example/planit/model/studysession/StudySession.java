package com.example.planit.model.studysession;

import com.example.planit.model.exam.Exam;
import com.example.planit.model.timeslot.TimeSlot;
import com.google.api.client.util.DateTime;

public class StudySession extends TimeSlot {

    private String courseName;
    private String description;
    private Exam examToStudyFor;

    public StudySession(DateTime start, DateTime end) {
        super(start, end);
        this.courseName = "";
        this.description = "";
        this.examToStudyFor = null;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Exam getExamToStudyFor() {
        return examToStudyFor;
    }

    public void setExamToStudyFor(Exam examToStudyFor) {
        this.examToStudyFor = examToStudyFor;
    }
}

package com.example.planit.model.mongo.user;

import com.example.planit.model.exam.Exam;

import java.util.List;

public class StudyPlan {
    private List<Exam> scannedExams;

    private String startDateTimeOfPlan;

    private String endDataTimeOfPlan;

    private int totalNumberOfStudySessions;

    public StudyPlan() {
    }

    public void setScannedExams(List<Exam> scannedExams) {
        this.scannedExams = scannedExams;
    }

    public void setStartDateTimeOfPlan(String startDateTimeOfPlan) {
        this.startDateTimeOfPlan = startDateTimeOfPlan;
    }

    public void setEndDataTimeOfPlan(String endDataTimeOfPlan) {
        this.endDataTimeOfPlan = endDataTimeOfPlan;
    }

    public void setTotalNumberOfStudySessions(int totalNumberOfStudySessions) {
        this.totalNumberOfStudySessions = totalNumberOfStudySessions;
    }
}

package com.example.planit.model.mongo.user;

import com.example.planit.model.exam.Exam;
import com.example.planit.model.exam.ExamClientRepresentation;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

public class StudyPlan {
    @Field(name = "scanned_exams")
    private List<ExamClientRepresentation> scannedExams;
    @Field(name = "start_datetime_of_plan")
    private String startDateTimeOfPlan;
    @Field(name = "end_datetime_of_plan")
    private String endDateTimeOfPlan;
    @Field(name = "number_of_sessions")
    private int totalNumberOfStudySessions;

    public StudyPlan() {
        this.scannedExams = new ArrayList<>();
    }

    public void setScannedExams(List<ExamClientRepresentation> scannedExams) {
        this.scannedExams = scannedExams;
    }

    public void setStartDateTimeOfPlan(String startDateTimeOfPlan) {
        this.startDateTimeOfPlan = startDateTimeOfPlan;
    }

    public void setEndDateTimeOfPlan(String endDateTimeOfPlan) {
        this.endDateTimeOfPlan = endDateTimeOfPlan;
    }

    public void setTotalNumberOfStudySessions(int totalNumberOfStudySessions) {
        this.totalNumberOfStudySessions = totalNumberOfStudySessions;
    }

    public List<ExamClientRepresentation> getScannedExams() {
        return scannedExams;
    }

    public String getStartDateTimeOfPlan() {
        return startDateTimeOfPlan;
    }

    public String getEndDateTimeOfPlan() {
        return endDateTimeOfPlan;
    }

    public int getTotalNumberOfStudySessions() {
        return totalNumberOfStudySessions;
    }

    public void convertAndSetScannedExamsAsClientRepresentation(List<Exam> scannedExams) {
        for (Exam currentScannedExam : scannedExams) {
            this.scannedExams.add(convertScannedExamToClientRepresentation(currentScannedExam));
        }
    }

    private static ExamClientRepresentation convertScannedExamToClientRepresentation(Exam scannedExamToBeConverted) {
        return new ExamClientRepresentation(scannedExamToBeConverted.getCourse().getCourseName(),
                scannedExamToBeConverted.getDateTime().toStringRfc3339());
    }
}

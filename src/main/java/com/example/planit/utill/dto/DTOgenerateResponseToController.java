package com.example.planit.utill.dto;

import com.example.planit.model.mongo.user.StudyPlan;
import org.springframework.http.HttpStatus;

public class DTOgenerateResponseToController extends DTOstatus {
    private final HttpStatus httpStatus;
    private final StudyPlan studyPlan;

    public DTOgenerateResponseToController(boolean isSucceed, String details, HttpStatus httpStatus, StudyPlan studyPlan) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
        this.studyPlan = studyPlan;
    }

    public DTOgenerateResponseToController(boolean isSucceed, String details, HttpStatus httpStatus) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
        this.studyPlan = new StudyPlan();
    }

    public StudyPlan getStudyPlan() {
        return studyPlan;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

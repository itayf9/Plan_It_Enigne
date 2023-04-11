package com.example.planit.utill.dto;

import com.example.planit.model.mongo.user.StudyPlan;
import org.springframework.http.HttpStatus;

public class DTOstudyPlanResponseToController extends DTOstatus {
    private final HttpStatus httpStatus;
    private final StudyPlan studyPlan;

    public DTOstudyPlanResponseToController(boolean isSucceed, String details, HttpStatus httpStatus, StudyPlan studyPlan) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
        this.studyPlan = studyPlan;
    }

    public DTOstudyPlanResponseToController(boolean isSucceed, String details, HttpStatus httpStatus) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
        this.studyPlan = null;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public StudyPlan getStudyPlan() {
        return studyPlan;
    }
}

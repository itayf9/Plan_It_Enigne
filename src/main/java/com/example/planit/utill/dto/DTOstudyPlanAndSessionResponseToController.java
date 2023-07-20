package com.example.planit.utill.dto;

import com.example.planit.model.mongo.user.StudyPlan;
import com.example.planit.model.studysession.StudySession;
import org.springframework.http.HttpStatus;

public class DTOstudyPlanAndSessionResponseToController extends DTOresponseToController {

    private final StudyPlan studyPlan;
    private final StudySession upComingSession;

    public DTOstudyPlanAndSessionResponseToController(boolean isSucceed, String details, HttpStatus httpStatus, StudyPlan studyPlan, StudySession upcomingSession) {
        super(isSucceed, details, httpStatus);
        this.studyPlan = studyPlan;
        this.upComingSession = upcomingSession;
    }

    public DTOstudyPlanAndSessionResponseToController(boolean isSucceed, String details, HttpStatus httpStatus) {
        super(isSucceed, details, httpStatus);
        this.studyPlan = null;
        upComingSession = null;
    }

    public StudyPlan getStudyPlan() {
        return studyPlan;
    }

    public StudySession getUpComingSession() {
        return upComingSession;
    }
}

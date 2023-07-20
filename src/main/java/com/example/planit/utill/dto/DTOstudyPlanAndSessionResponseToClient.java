package com.example.planit.utill.dto;

import com.example.planit.model.mongo.user.StudyPlan;
import com.example.planit.model.studysession.StudySession;

public class DTOstudyPlanAndSessionResponseToClient extends DTOstatus {

    private final StudyPlan studyPlan;

    private final StudySession upComingSession;

    public DTOstudyPlanAndSessionResponseToClient(boolean isSucceed, String details, StudyPlan studyPlan, StudySession ucomingSession) {
        super(isSucceed, details);
        this.studyPlan = studyPlan;
        this.upComingSession = ucomingSession;
    }

    public StudyPlan getStudyPlan() {
        return studyPlan;
    }

    public StudySession getUpComingSession() {
        return upComingSession;
    }
}

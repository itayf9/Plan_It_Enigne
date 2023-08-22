package com.example.planit.utill.dto;

import com.example.planit.model.event.EventClientRepresentation;
import com.example.planit.model.mongo.user.StudyPlan;

public class DTOstudyPlanAndSessionResponseToClient extends DTOstatus {

    private final StudyPlan studyPlan;

    private final EventClientRepresentation upComingSession;

    public DTOstudyPlanAndSessionResponseToClient(boolean isSucceed, String details, StudyPlan studyPlan, EventClientRepresentation upComingSession) {
        super(isSucceed, details);
        this.studyPlan = studyPlan;
        this.upComingSession = upComingSession;
    }

    public StudyPlan getStudyPlan() {
        return studyPlan;
    }

    public EventClientRepresentation getUpComingSession() {
        return upComingSession;
    }
}

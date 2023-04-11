package com.example.planit.utill.dto;

import com.example.planit.model.mongo.user.StudyPlan;

public class DTOstudyPlanResponseToClient extends DTOstatus {

    private final StudyPlan studyPlan;

    public DTOstudyPlanResponseToClient(boolean isSucceed, String details, StudyPlan studyPlan) {
        super(isSucceed, details);
        this.studyPlan = studyPlan;
    }

    public DTOstudyPlanResponseToClient(boolean isSucceed, String details) {
        super(isSucceed, details);
        this.studyPlan = null;
    }

    public StudyPlan getStudyPlan() {
        return studyPlan;
    }
}

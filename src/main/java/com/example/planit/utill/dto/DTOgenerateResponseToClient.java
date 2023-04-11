package com.example.planit.utill.dto;

import com.example.planit.model.mongo.user.StudyPlan;

public class DTOgenerateResponseToClient extends DTOstatus {

    private final StudyPlan studyPlan;

    public DTOgenerateResponseToClient(boolean isSucceed, String details, StudyPlan studyPlan) {
        super(isSucceed, details);
        this.studyPlan = studyPlan;
    }

    public StudyPlan getStudyPlan() {
        return studyPlan;
    }
}

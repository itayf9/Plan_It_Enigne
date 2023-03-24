package com.example.planit.utill.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DTOloginResponseToClient extends DTOstatus {

    @JsonProperty("sub")
    private String subjectID;

    public DTOloginResponseToClient(boolean isSucceed, String details, String subjectID) {
        super(isSucceed, details);
        this.subjectID = subjectID;
    }
}

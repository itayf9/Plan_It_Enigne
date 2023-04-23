package com.example.planit.utill.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DTOloginResponseToClient extends DTOstatus {

    @JsonProperty("sub")
    private String subjectID;

    @JsonProperty("isAdmin")
    private boolean isAdmin;

    public DTOloginResponseToClient(boolean isSucceed, String details, String subjectID, boolean isAdmin) {
        super(isSucceed, details);
        this.subjectID = subjectID;
        this.isAdmin = isAdmin;
    }
}

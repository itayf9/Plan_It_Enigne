package com.example.planit.utill.dto;

import com.example.planit.model.mongo.user.UserClientRepresentation;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DTOuserClientRepresentation extends DTOstatus {
    @JsonProperty("user")
    UserClientRepresentation userClientRepresentation;

    public DTOuserClientRepresentation(boolean isSucceed, String details, UserClientRepresentation userClientRepresentation) {
        super(isSucceed, details);
        this.userClientRepresentation = userClientRepresentation;
    }

    public DTOuserClientRepresentation(boolean isSucceed, String details) {
        super(isSucceed, details);
        this.userClientRepresentation = null;
    }

    public UserClientRepresentation getUserClientRepresentation() {
        return userClientRepresentation;
    }
}

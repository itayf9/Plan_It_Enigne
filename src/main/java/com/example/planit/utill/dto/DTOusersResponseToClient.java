package com.example.planit.utill.dto;

import com.example.planit.model.mongo.user.UserClientRepresentation;

import java.util.List;

public class DTOusersResponseToClient extends DTOstatus {
    List<UserClientRepresentation> users;

    public DTOusersResponseToClient(boolean isSucceed, String details, List<UserClientRepresentation> users) {
        super(isSucceed, details);
        this.users = users;
    }

    public List<UserClientRepresentation> getUsers() {
        return users;
    }
}

package com.example.planit.utill.dto;

import com.example.planit.model.mongo.user.UserClientRepresentation;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class DTOusersResponseToController extends DTOstatus {
    private final HttpStatus httpStatus;
    private final List<UserClientRepresentation> users;

    public DTOusersResponseToController(boolean isSucceed, String details, HttpStatus httpStatus, List<UserClientRepresentation> users) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
        this.users = users;
    }

    public DTOusersResponseToController(boolean isSucceed, String details, HttpStatus httpStatus) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
        this.users = new ArrayList<>();
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public List<UserClientRepresentation> getUsers() {
        return users;
    }
}

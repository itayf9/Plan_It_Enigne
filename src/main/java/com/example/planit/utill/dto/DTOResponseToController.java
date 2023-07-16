package com.example.planit.utill.dto;

import org.springframework.http.HttpStatus;

public class DTOResponseToController extends DTOstatus {

    private final HttpStatus httpStatus;

    public DTOResponseToController(boolean isSucceed, String details, HttpStatus httpStatus) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

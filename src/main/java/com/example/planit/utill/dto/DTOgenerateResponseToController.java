package com.example.planit.utill.dto;

import org.springframework.http.HttpStatus;

public class DTOgenerateResponseToController extends DTOstatus {
    private HttpStatus httpStatus;

    public DTOgenerateResponseToController(boolean isSucceed, String details, HttpStatus httpStatus) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

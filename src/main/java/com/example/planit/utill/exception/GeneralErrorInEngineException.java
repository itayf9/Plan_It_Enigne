package com.example.planit.utill.exception;

import org.springframework.http.HttpStatus;

public class GeneralErrorInEngineException extends Exception {

    private final boolean isSucceed;
    private final String details;
    private final HttpStatus httpStatus;

    public GeneralErrorInEngineException(boolean isSucceed, String details, HttpStatus httpStatus) {
        super();
        this.isSucceed = isSucceed;
        this.details = details;
        this.httpStatus = httpStatus;
    }

    public GeneralErrorInEngineException(String message, boolean isSucceed, String details, HttpStatus httpStatus) {
        super(message);
        this.isSucceed = isSucceed;
        this.details = details;
        this.httpStatus = httpStatus;
    }

    public GeneralErrorInEngineException(String message, Throwable cause, boolean isSucceed, String details, HttpStatus httpStatus) {
        super(message, cause);
        this.isSucceed = isSucceed;
        this.details = details;
        this.httpStatus = httpStatus;
    }

    public GeneralErrorInEngineException(Throwable cause, boolean isSucceed, String details, HttpStatus httpStatus) {
        super(cause);
        this.isSucceed = isSucceed;
        this.details = details;
        this.httpStatus = httpStatus;
    }

    public GeneralErrorInEngineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, boolean isSucceed, String details, HttpStatus httpStatus) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.isSucceed = isSucceed;
        this.details = details;
        this.httpStatus = httpStatus;
    }

    public boolean isSucceed() {
        return isSucceed;
    }

    public String getDetails() {
        return details;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

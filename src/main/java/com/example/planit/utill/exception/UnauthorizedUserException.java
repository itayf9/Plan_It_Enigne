package com.example.planit.utill.exception;

public class UnauthorizedUserException extends RuntimeException {

    private final String subjectID;

    public UnauthorizedUserException(String subjectID) {
        super();
        this.subjectID = subjectID;
    }

    public UnauthorizedUserException(String message, String subjectID) {
        super(message);
        this.subjectID = subjectID;
    }

    public UnauthorizedUserException(String message, Throwable cause, String subjectID) {
        super(message, cause);
        this.subjectID = subjectID;
    }

    public UnauthorizedUserException(Throwable cause, String subjectID) {
        super(cause);
        this.subjectID = subjectID;
    }

    protected UnauthorizedUserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String subjectID) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.subjectID = subjectID;
    }

    public String getSubjectID() {
        return subjectID;
    }
}

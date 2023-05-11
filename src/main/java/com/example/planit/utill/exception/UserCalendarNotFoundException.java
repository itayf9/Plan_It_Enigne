package com.example.planit.utill.exception;

public class UserCalendarNotFoundException extends RuntimeException {

    private String calendarError;

    public UserCalendarNotFoundException(String calendarError) {
        this.calendarError = calendarError;
    }

    public UserCalendarNotFoundException(String message, String calendarError) {
        super(message);
        this.calendarError = calendarError;
    }

    public UserCalendarNotFoundException(String message, Throwable cause, String calendarError) {
        super(message, cause);
        this.calendarError = calendarError;
    }

    public UserCalendarNotFoundException(Throwable cause, String calendarError) {
        super(cause);
        this.calendarError = calendarError;
    }

    public UserCalendarNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String calendarError) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.calendarError = calendarError;
    }

    public String getCalendarError() {
        return calendarError;
    }
}

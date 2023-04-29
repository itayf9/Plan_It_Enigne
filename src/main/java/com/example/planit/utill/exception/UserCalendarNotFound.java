package com.example.planit.utill.exception;

public class UserCalendarNotFound extends RuntimeException{

    private String calendarError;

    public UserCalendarNotFound(String calendarError) {
        this.calendarError = calendarError;
    }

    public UserCalendarNotFound(String message, String calendarError) {
        super(message);
        this.calendarError = calendarError;
    }

    public UserCalendarNotFound(String message, Throwable cause, String calendarError) {
        super(message, cause);
        this.calendarError = calendarError;
    }

    public UserCalendarNotFound(Throwable cause, String calendarError) {
        super(cause);
        this.calendarError = calendarError;
    }

    public UserCalendarNotFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String calendarError) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.calendarError = calendarError;
    }

    public String getCalendarError() {
        return calendarError;
    }
}

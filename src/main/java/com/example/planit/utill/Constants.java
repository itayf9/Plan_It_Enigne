package com.example.planit.utill;

public class Constants {

    public static final long ONE_MONTH_IN_MILLIS = 2592000000L;

    public static final long ONE_HOUR_IN_MILLIS = 3599000;

    public static final int MILLIS_TO_HOUR = 3600000;

    public static final long MINUTES_TO_MILLIS = 60000;

    public static final int MINUTE_AS_SECONDS = 60;

    public static final int SPACE_LIMIT_BETWEEN_EVENTS_IN_MINUTES = 60;

    /**
     * Application name.
     */
    public static final String CALENDAR_HOLIDAYS_ID_IN_GOOGLE = "iw.jewish#holiday@group.v.calendar.google.com";

    public static final String APPLICATION_NAME = "PlanIt";

    public static final String PLAN_IT_WEB_PRODUCTION_URI = "https://plan-it-web-seven.vercel.app";

    public static final String PLANIT_CALENDAR_SUMMERY_NAME = "PlanIt Calendar";

    public static final String EVENT_SUMMERY_PREFIX = "למידה ל";

    public static final String EVENT_DESCRIPTION_PRACTISE_PREV_EXAMS = "תרגול מבחנים";

    public static final String ISRAEL_HOLIDAYS_CODE = "il";

    public static final String ISRAEL_TIME_ZONE = "Asia/Jerusalem";

    public static final String ERROR_NO_EXAMS_FOUND = "No Exams Found.";

    public static final String NO_PROBLEM = "No Problem Found";

    public static final String UNHANDLED_FULL_DAY_EVENTS = "Unhandled Full Days Events.";

    public static final String ERROR_USER_NOT_FOUND = "The User Is Not Found";

    public static final String CALENDAR_LOGGER_NAME = "calendar-logger";

    public static final String REGISTER = "Register";

    public static final String LOGIN = "Login";
    public static final String ERROR_UNAUTHORIZED_USER = "The user is unauthorized";

    public static final String ERROR_NO_CALENDAR_SCOPE_GRANTED = "Calendar scope could not be found.";
    public static final String ERROR_INVALID_GRANT = "Invalid Grant";
    public static final String ERROR_FROM_GOOGLE_API_EXECUTE = "General exception google api execute";
    public static final String EXAMS_CALENDAR_SUMMERY_NAME = "יומן אישי מתחנת המידע";
    public static final CharSequence EXAM_KEYWORD_TO_IDENTIFY_EXAMS = "מבחן";
    public static final String ERROR_DEFAULT = "Some Internal Server Error";
    public static final String ERROR_ILLEGAL_CHARACTERS_IN_AUTH_CODE = "There are illegal characters in the auth code";
    public static final String ERROR_CALENDRIFIC_EXCEPTION = "Error From Callendrific";
    public static String ERROR_NO_VALID_ACCESS_TOKEN = "Access Token not valid anymore.";
    public static String ERROR_COURSE_ALREADY_EXIST = "Course already exists in database";
    public static String ERROR_COURSE_NOT_FOUND = "Course does not exist in database";

    public static String ERROR_COLLEGE_CALENDAR_NOT_FOUND = "College calendar does not found";

    public static String ERROR_PLANIT_CALENDAR_NOT_FOUND = "PlanIT calendar does not found";

    public static String ERROR_GENERATE_DAYS_NOT_VALID = "The generate days are no longer valid";
    public static String ERROR_GENERATE_NOT_PERFORMED = "generate wasn't performed";
}

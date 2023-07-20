package com.example.planit.utill;

import com.example.planit.model.studysession.StudySession;
import com.example.planit.utill.dto.DTOstartAndEndOfInterval;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class Utility {

    public static String buildExceptionMessage(Exception e) {
        StringBuilder buildExceptionMessageBuilder = new StringBuilder();

        buildExceptionMessageBuilder.append("Exception ")
                .append(e.getClass()).append(": ").append(e.getMessage())
                .append("\n");

        StackTraceElement[] stackTraceElements = e.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            buildExceptionMessageBuilder.append("          at ")
                    .append(stackTraceElement.toString()).append("\n");
        }

        return buildExceptionMessageBuilder.toString();
    }

    /**
     * checks if the instants are on the same date (Year, Month, Day)
     *
     * @param firstInstant  the first Instant
     * @param secondInstant the second Instant
     * @return true if both are on the same date, false otherwise
     */
    public static boolean isSameDay(Instant firstInstant, Instant secondInstant) {

        // makes both of the instants with the same hour
        Instant fixedFirstInstant = firstInstant.with(ChronoField.HOUR_OF_DAY, 0);
        Instant fixedSecondInstant = secondInstant.with(ChronoField.HOUR_OF_DAY, 0);

        return fixedFirstInstant.until(fixedSecondInstant, ChronoUnit.HOURS) == 0;
    }

    /**
     * for a specific instant, finds the instants of the userStudyStartTime and userStudyEndTime,
     * that appear on the same date of the specific instant.
     * this represents the interval (e.g. 8:00 to 22:00) of the current day, for a specific instant
     *
     * @param currentDay         an {@link Instant} that we want to find its interval
     * @param userStudyStartTime the study start time (int format, e.g. 800)
     * @param userStudyEndTime   the study end time (int format, e.g. 2200)
     * @return a {@link DTOstartAndEndOfInterval} represents the current userStudyStartTime and userStudyEndTime values
     */
    public static DTOstartAndEndOfInterval getCurrentInterval(Instant currentDay, int userStudyStartTime, int userStudyEndTime) {

        int startStudyHours = convertUserStudyTimeToHours(userStudyStartTime);
        int startStudyMinute = convertUserStudyTimeToMinute(userStudyStartTime);
        int endStudyHours = convertUserStudyTimeToHours(userStudyEndTime);
        int endStudyMinute = convertUserStudyTimeToMinute(userStudyEndTime);

        // create an instant of the start of the interval
        Instant startOfInterval = currentDay
                .atZone(ZoneId.of(Constants.ISRAEL_TIME_ZONE))
                .withHour(startStudyHours)
                .withMinute(startStudyMinute)
                .withSecond(0)
                .withNano(0)
                .toInstant();

        // create an instant of the end of the interval
        Instant endOfInterval = currentDay
                .atZone(ZoneId.of(Constants.ISRAEL_TIME_ZONE))
                .withHour(endStudyHours)
                .withMinute(endStudyMinute)
                .withSecond(0)
                .withNano(0)
                .toInstant();

        return new DTOstartAndEndOfInterval(startOfInterval, endOfInterval);
    }

    /**
     * extracts the day out of the CurrentDay Instant object and return an object with the next day.
     *
     * @param CurrentDay is an Instant object that contains a current day.
     * @return Instant object that contains the next day.
     */
    public static Instant addDayToCurrentInstant(Instant CurrentDay) {
        return CurrentDay.plus(1, ChronoUnit.DAYS);
    }

    /**
     * extracts the hours out of the userStudyTime
     *
     * @param userStudyTime is an int that represents user study time (e.g. 800 is 8:00).
     * @return a long number that represents only the hours from user study time.
     */
    public static int convertUserStudyTimeToHours(int userStudyTime) {
        int hours;

        hours = userStudyTime / 100;

        if (hours > 23) {
            throw new IllegalArgumentException();
        }

        return hours;
    }

    /**
     * extracts the minutes out of the userStudyTime
     *
     * @param userStudyTime is an int that represents user study time (e.g. 800 is 8:00).
     * @return a long number that represents only the minutes from user study time.
     */
    public static int convertUserStudyTimeToMinute(int userStudyTime) {
        int minute;

        minute = userStudyTime % 100;

        if (minute > 59) {
            throw new IllegalArgumentException();
        }
        return minute;
    }

    /**
     * get the start and end of the study time form the user and return the total minutes.
     *
     * @param userStartStudyTime get the start study time of the user
     * @param userEndStudyTime   get the end study time of the user
     * @return return the total minutes of the study time the user set.
     */
    public static int getTotalMinutesOfStudyInDay(int userStartStudyTime, int userEndStudyTime) {
        int startHourStudy = convertUserStudyTimeToHours(userStartStudyTime);
        int startMinutesStudy = convertUserStudyTimeToMinute(userStartStudyTime);

        int endHourStudy = convertUserStudyTimeToHours(userEndStudyTime);
        int endMinutesStudy = convertUserStudyTimeToMinute(userEndStudyTime);

        if (endMinutesStudy - startMinutesStudy < 0) {
            endMinutesStudy = endMinutesStudy + 60;
            endHourStudy = endHourStudy - 1;
        }
        int totalMinutes = endMinutesStudy - startMinutesStudy;
        int totalHours = endHourStudy - startHourStudy;

        return totalMinutes + totalHours * 60;
    }

    /**
     * @param currentTime       Instant represent current time
     * @param isRoundedForwards a boolean to tell which way to round.
     * @return Instant with the rounded value of minutes
     */
    public static Instant roundInstantMinutesTime(Instant currentTime, boolean isRoundedForwards) {

        int timeUnit1 = 0;
        int timeUnit2 = 15;
        int timeUnit3 = 30;
        int timeUnit4 = 45;
        int timeUnit5 = 60;

        ZonedDateTime result = currentTime.atZone(ZoneId.of(Constants.ISRAEL_TIME_ZONE));
        int currentMinutes = result.getMinute();

        // case 1 where minutes value is in between 1 and 14
        if (currentMinutes > timeUnit1 && currentMinutes < timeUnit2) {
            if (isRoundedForwards) {
                result = result.withMinute(timeUnit2);
            } else {
                result = result.withMinute(timeUnit1);
            }

            // case 2 where minutes value is in between 16 and 29
        } else if (currentMinutes > timeUnit2 && currentMinutes < timeUnit3) {
            if (isRoundedForwards) {
                result = result.withMinute(timeUnit3);
            } else {
                result = result.withMinute(timeUnit2);
            }

            // case 3 where minutes value is in between 31 and 44
        } else if (currentMinutes > timeUnit3 && currentMinutes < timeUnit4) {
            if (isRoundedForwards) {
                result = result.withMinute(timeUnit4);

            } else {
                result = result.withMinute(timeUnit3);
            }

            // case 4 where minutes value is in between 46 and 59
        } else if (currentMinutes > timeUnit4 && currentMinutes < timeUnit5) {
            if (isRoundedForwards) {
                result = result.plusHours(1).withMinute(timeUnit1);
            } else {
                result = result.withMinute(timeUnit4);
            }
        }
        return result.toInstant();
    }

    /**
     * converts a full day event to a regular event, by setting the DateTime field to the original Date field value.
     *
     * @param fullDayEventToBeConverted a {@link Event} that is a full day event and should be converted to a regular event
     * @return a {@link Event} that is the converted event
     */
    public static Event convertFullDayEventToRegularEvent(Event fullDayEventToBeConverted) {

        DateTime startDateOfFullDayEvent = fullDayEventToBeConverted.getStart().getDate();// 00:00
        DateTime endDateOfFullDayEvent = fullDayEventToBeConverted.getEnd().getDate();// 00:00

        fullDayEventToBeConverted.getStart().setDateTime(startDateOfFullDayEvent);
        fullDayEventToBeConverted.getEnd().setDateTime(endDateOfFullDayEvent);

        fullDayEventToBeConverted.getStart().setDate(null);
        fullDayEventToBeConverted.getEnd().setDate(null);

        return fullDayEventToBeConverted;
    }

    /**
     * convert event to study session (without examToStudyFor member).
     * @param event event that convert to study session
     * @return study session after extract the information from the event
     */
    public static StudySession convertEventToUpcomingStudySession(Event event){

        return new StudySession(event.getStart().getDateTime(),
                event.getEnd().getDateTime(),
                event.getDescription(),
                event.getSummary());
    }
}

package com.example.lamivhan.utill;

import com.example.lamivhan.utill.dto.DTOstartAndEndOfInterval;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class Utility {

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
        long startStudyHours = convertUserStudyTimeToHours(userStudyStartTime);
        long startStudyMinute = convertUserStudyTimeToMinute(userStudyStartTime);
        long endStudyHours = convertUserStudyTimeToHours(userStudyEndTime);
        long endStudyMinute = convertUserStudyTimeToMinute(userStudyEndTime);
        // create an instant of the start of the interval
        Instant startOfInterval = currentDay.with(ChronoField.HOUR_OF_DAY, startStudyHours);
        startOfInterval = startOfInterval.with(ChronoField.MINUTE_OF_DAY, startStudyMinute);
        startOfInterval = startOfInterval.with(ChronoField.SECOND_OF_DAY, 0);
        // create an instant of the end of the interval
        Instant endOfInterval = currentDay.with(ChronoField.HOUR_OF_DAY, endStudyHours);
        endOfInterval = endOfInterval.with(ChronoField.MINUTE_OF_DAY, endStudyMinute);
        endOfInterval = endOfInterval.with(ChronoField.SECOND_OF_DAY, 0);

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
    private static long convertUserStudyTimeToHours(int userStudyTime) {
        long hours;

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
    private static long convertUserStudyTimeToMinute(int userStudyTime) {
        long minute;

        minute = userStudyTime % 100;

        if (minute > 59) {
            throw new IllegalArgumentException();
        }
        return minute;
    }
}

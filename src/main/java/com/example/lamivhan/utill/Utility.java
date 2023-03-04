package com.example.lamivhan.utill;

import com.example.lamivhan.utill.dto.DTOstartAndEndOfInterval;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private static int convertUserStudyTimeToHours(int userStudyTime) {
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
    private static int convertUserStudyTimeToMinute(int userStudyTime) {
        int minute;

        minute = userStudyTime % 100;

        if (minute > 59) {
            throw new IllegalArgumentException();
        }
        return minute;
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

        ZonedDateTime result = currentTime.atZone(ZoneId.of(Constants.ISRAEL_TIME_ZONE));
        int currentMinutes = result.getMinute();

        // case 1 where minutes value is in between 0 and 15
        if (currentMinutes >= timeUnit1 && currentMinutes <= timeUnit2) {
            if (isRoundedForwards) {
                result = result.withMinute(timeUnit2);
            } else {
                return Instant.ofEpochMilli(currentTime.toEpochMilli()).with(ChronoField.MINUTE_OF_HOUR, timeUnit1);
            }

            // case 2 where minutes value is in between 15 and 30
        } else if (currentMinutes >= timeUnit2 && currentMinutes <= timeUnit3) {
            if (isRoundedForwards) {
                return Instant.ofEpochMilli(currentTime.toEpochMilli()).with(ChronoField.MINUTE_OF_HOUR, timeUnit3);
            } else {
                return Instant.ofEpochMilli(currentTime.toEpochMilli()).with(ChronoField.MINUTE_OF_HOUR, timeUnit2);
            }

            // case 3 where minutes value is in between 30 and 45
        } else if (currentMinutes >= timeUnit3 && currentMinutes <= timeUnit4) {
            if (isRoundedForwards) {
                return Instant.ofEpochMilli(currentTime.toEpochMilli()).with(ChronoField.MINUTE_OF_HOUR, timeUnit4);
            } else {
                return Instant.ofEpochMilli(currentTime.toEpochMilli()).with(ChronoField.MINUTE_OF_HOUR, timeUnit3);
            }

            // case 4 where minutes value is in between 45 and 0
        } else {
            if (isRoundedForwards) {
                return Instant.ofEpochMilli(currentTime.toEpochMilli()).with(ChronoField.MINUTE_OF_HOUR, timeUnit1);
            } else {
                return Instant.ofEpochMilli(currentTime.toEpochMilli()).with(ChronoField.MINUTE_OF_HOUR, timeUnit4);
            }
        }
        return result.toInstant();
    }
}

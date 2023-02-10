package com.example.lamivhan.utill;

import com.example.lamivhan.utill.dto.DTOstartAndEndOfInterval;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class Utility {

    public static boolean isSameDay(Instant firstInstant, Instant secondInstant) {

        // makes both of the instants with the same hour
        Instant fixedFirstInstant = firstInstant.with(ChronoField.HOUR_OF_DAY, 0);
        Instant fixedSecondInstant = secondInstant.with(ChronoField.HOUR_OF_DAY, 0);

        return fixedFirstInstant.until(fixedSecondInstant, ChronoUnit.HOURS) == 0;
    }

    public static DTOstartAndEndOfInterval getCurrentInterval(Instant currentDay,int userStudyStartTime ,int userStudyEndTime)
    {
        long startStudyHours = convertUserStudyTimeToHours(userStudyStartTime);
        long startStudyMinute = convertUserStudyTimeToMinute(userStudyStartTime);
        long endStudyHours = convertUserStudyTimeToHours(userStudyEndTime);
        long endStudyMinute = convertUserStudyTimeToMinute(userStudyEndTime);
        // create an instant of the start of the interval
        Instant startOfInterval = currentDay.with(ChronoField.HOUR_OF_DAY, startStudyHours);
        startOfInterval = startOfInterval.with(ChronoField.MINUTE_OF_DAY,startStudyMinute);
        startOfInterval = startOfInterval.with(ChronoField.SECOND_OF_DAY,0);
        // create an instant of the end of the interval
        Instant endOfInterval = currentDay.with(ChronoField.HOUR_OF_DAY, endStudyHours);
        endOfInterval = endOfInterval.with(ChronoField.MINUTE_OF_DAY,endStudyMinute);
        endOfInterval = endOfInterval.with(ChronoField.SECOND_OF_DAY,0);

        return new DTOstartAndEndOfInterval(startOfInterval,endOfInterval);
    }

    public static Instant addDayToCurrentInstant(Instant CurrentDay)
    {
       return  CurrentDay.plus(1, ChronoUnit.DAYS);
    }

    private static long convertUserStudyTimeToHours(int userStudyTime)
    {
        long hours;

        hours = userStudyTime / 100;

        if (hours > 23)
        {
            throw new IllegalArgumentException();
        }

        return hours;
    }

    private static long convertUserStudyTimeToMinute(int userStudyTime)
    {
        long minute;

        minute = userStudyTime % 100;

        if (minute > 59)
        {
            throw new IllegalArgumentException();
        }
        return minute;
    }
}

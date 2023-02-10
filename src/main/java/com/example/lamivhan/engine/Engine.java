package com.example.lamivhan.engine;

import com.example.lamivhan.model.timeslot.TimeSlot;
import com.example.lamivhan.model.user.User;
import com.example.lamivhan.utill.Constants;
import com.example.lamivhan.utill.Utility;
import com.example.lamivhan.utill.dto.DTOfreetime;
import com.example.lamivhan.utill.dto.DTOstartAndEndOfInterval;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Engine {
    // need to get user object
    public static DTOfreetime getFreeSlots(List<Event> userEvents, User user) {

        List<TimeSlot> userFreeTimeSlots = new ArrayList<>();
        Date now = new Date();

        // get the time before the first event
        if (userEvents.size() > 0) {
            long CurrentTimeOfPress = now.getTime();
            long startOfCurrentEvent = userEvents.get(0).getStart().getDateTime().getValue();
            userFreeTimeSlots.add(new TimeSlot(new DateTime(CurrentTimeOfPress), new DateTime(startOfCurrentEvent)));
        }

        // get all free time slots in the event
        for (int i = 0; i < userEvents.size() - 1; i++) {
            // get start of event in i+1 place
            long startOfNextEvent = userEvents.get(i + 1).getStart().getDateTime().getValue();
            // get the end of event in i place
            long endOfCurrentEvent = userEvents.get(i).getEnd().getDateTime().getValue();
            if (endOfCurrentEvent < startOfNextEvent) {
                //check if we have time to add, add to list of free time.
                userFreeTimeSlots.add(new TimeSlot(new DateTime(endOfCurrentEvent), new DateTime(startOfNextEvent)));
            }
        }

        return adjustFreeSlotsList(userFreeTimeSlots, user);
    }

    private static DTOfreetime adjustFreeSlotsList(List<TimeSlot> userFreeTimeSlots, User user) {
        int totalFreeTime = 0;
        List<TimeSlot> adjustedUserFreeSlots = new ArrayList<>();

        // gets user's preferences
        int userStudyStartTime = user.getUserPreferences().getUserStudyStartTime();
        int userStudyEndTime = user.getUserPreferences().getUserStudyEndTime();
        int userStudySessionTime = user.getUserPreferences().getStudySessionTime();

        // goes through the raw time slots
        for (TimeSlot userFreeTimeSlot : userFreeTimeSlots) {

            // gets current slot end and start
            Instant startOfCurrentSlot = Instant.ofEpochMilli(userFreeTimeSlot.getStart().getValue());
            Instant endOfCurrentSlot = Instant.ofEpochMilli(userFreeTimeSlot.getEnd().getValue());

            // finds the first place of user study start time (e.g. first 8:00)
            // finds the first place of user study end time (e.g. first 22:00)
            DTOstartAndEndOfInterval currentInterval = Utility.getCurrentInterval(startOfCurrentSlot, userStudyStartTime, userStudyEndTime);
            Instant userStudyStartFirst = currentInterval.getStartOfInterval();
            Instant userStudyEndFirst = currentInterval.getEndOfInterval();

            Instant selectedStart;
            Instant selectedEnd;

            // we take the min(userStudyEndFirst,endOfCurrentSlot)
            if (userStudyEndFirst.isAfter(endOfCurrentSlot)) { // the first 22:00 is after the end of slot
                selectedEnd = endOfCurrentSlot;
            } else {
                selectedEnd = userStudyEndFirst;
            }

            // we take the max(userStudyStartFirst, startOfCurrentSlot)
            if (userStudyStartFirst.isBefore(startOfCurrentSlot)) { // the first 08:00 is before the start of slot
                selectedStart = startOfCurrentSlot;
            } else {
                selectedStart = userStudyStartFirst;
            }

            if (selectedStart.until(selectedEnd, ChronoUnit.HOURS) >= userStudySessionTime) {
                // adds the study time of the first day
                adjustedUserFreeSlots.add(new TimeSlot(new DateTime(selectedStart.toEpochMilli()), new DateTime(selectedEnd.toEpochMilli())));
                totalFreeTime += (selectedEnd.toEpochMilli() - selectedStart.toEpochMilli()) / Constants.MILLIS_TO_HOUR;
            }

            // finds the next place of user study start time (e.g. next 8:00)
            // finds the next place of user study end time (e.g. next 22:00)
            Instant userStudyStartNext = Utility.addDayToCurrentInstant(userStudyStartFirst);
            Instant userStudyEndNext = Utility.addDayToCurrentInstant(userStudyEndFirst);

            while (userStudyEndNext.isBefore(endOfCurrentSlot)) {
                // adds a full day study for the next day
                adjustedUserFreeSlots.add(new TimeSlot(new DateTime(userStudyStartNext.toEpochMilli()), new DateTime(userStudyEndNext.toEpochMilli())));
                totalFreeTime += (userStudyEndNext.toEpochMilli() - userStudyStartNext.toEpochMilli()) / Constants.MILLIS_TO_HOUR;

                // finds the next place of user study start time (e.g. next 8:00)
                // finds the next place of user study end time (e.g. next 22:00)
                userStudyStartNext = Utility.addDayToCurrentInstant(userStudyStartFirst);
                userStudyEndNext = Utility.addDayToCurrentInstant(userStudyEndFirst);
            }

            if ((userStudyStartNext.isBefore(endOfCurrentSlot))
                    && (userStudyStartNext.until(endOfCurrentSlot, ChronoUnit.HOURS) >= userStudySessionTime)) {
                // adds the study time of the last day
                adjustedUserFreeSlots.add(new TimeSlot(new DateTime(userStudyStartNext.toEpochMilli()), new DateTime(endOfCurrentSlot.toEpochMilli())));
                totalFreeTime += (endOfCurrentSlot.toEpochMilli() - userStudyStartNext.toEpochMilli()) / Constants.MILLIS_TO_HOUR;
            }

            /*// creates two "Instant"s that represent the day of the startOfCurrentSlot with different hours
            // an Instant with user's study start time (e.g. instance of 08:00 at the same day)
            // an Instant with user's study end time (e.g. instance of 22:00 at same day)
            Instant insOfUserStudyStartTime = Instant.ofEpochMilli(startOfCurrentSlot);
            insOfUserStudyStartTime.with(ChronoField.HOUR_OF_DAY, userStudyStartTime);

            Instant insOfUserStudyEndTime = Instant.ofEpochMilli(startOfCurrentSlot);
            insOfUserStudyEndTime.with(ChronoField.HOUR_OF_DAY, userStudyEndTime);


            // here we separate to a few cases

            // case 1 when slot starts and ends at the same day
            if (startOfCurrentSlot >= insOfUserStudyStartTime.toEpochMilli()
                    && endOfCurrentSlot <= insOfUserStudyEndTime.toEpochMilli()) {
                // nothing to adjust here
                // adds the slot to the list
                adjustedUserFreeSlots.add(new TimeSlot(new DateTime(startOfCurrentSlot), new DateTime(endOfCurrentSlot)));
                totalFreeTime += (endOfCurrentSlot - startOfCurrentSlot) / Constants.MILLIS_TO_HOUR;*/

            /*

            same day
                 start                              end
            V|----8:00---9:00------------12:00------22:00-----| need to get (9:00,12:00)
                 start                   end
            V|----8:00---9:00------------22:00------23:00-----| need to get (9:00,22:00)
                       start                        end
            V|----6:00---8:00------------12:00------22:00-----| need to get (8:00,12:00)
                        start            end
            V|----6:00---8:00------------22:00------23:00-----| need to get (8:00,22:00)

            diffrent days
                        start slot                    end slot
            V|----8:00------9:00------22:00----8:00------12:00------22:00-----| need to get (9:00,22:00) and (8:00,12:00)
                  7\7       7\7       7\7     8\7        8\7       8\7
                         start slot                             end slot
            V|----8:00-------9:00------22:00-----8:00----22:00------23:00-----| need to get (9:00,22:00) and (8:00,22:00)
                  7\7        7\7       7\7      8\7      8\7       8\7
                 start slot                           end slot
            V|------6:00-------8:00----22:00----8:00----20:00------22:00------| need to get (8:00,22:00) and (8:00,20:00)
                  7\7          7\7     7\7      8\7      8\7       8\7
               start slot                                     end slot
            V|------6:00-----8:00----22:00----8:00----22:00------23:00-------| need to get (8:00,22:00) and (8:00,22:00)
                  7\7        7\7     7\7      8\7      8\7       8\7

                      v|---------START----X-----Y-----END----------| // first case
                      v|-----X---START------Y---------END----------| // second case
                      v|---------START------X---------END-Y--23:59-| else case
                       |--X------START----------------END-Y--23:59-| // need to complete, now it adds the slot twice (thinks it's both the first interval and second interval)

                       if (Y< END && X > START )
                       else if (Y< END && X <= START)
                       else (Y >= END)

                       if (Y< END && X > START )
                       else if (Y< END && X <= START)
                       else if (Y >= END && X > START)
                       else (Y>= END && X <= START)



                       if (X,Y same day) {
                        if (Y< END && X > START )
                        else if (Y< END && X <= START)
                        else (oshri's case)
                      } else (X,Y not same day) {
                        // general case
                      }






                     |---------------------X-------------------------Y--------------------|



                     START = find first place of START
                     END = find first place of END


                     if END > Y // the first 22:00 is after the end of slot  // we take the min(END,Y)
                        RealEND = Y
                     else
                        RealEND = END

                     if START < X // the first 08:00 is before the start of slot  // we take the max(START,X)
                        RealSTART = X
                     else
                        RealSTART = START

                     if RealEnd - RealStart >= SESSION_TIME
                        take (RealStart, RealEnd)

                     STARTnext = find next place of START
                     ENDnext = find next place of END

                     while (STARTnext > END && ENDnext < Y) {
                        take (STARTnext, ENDnext)
                        END = ENDnext
                        STARTnext = find next place of START
                        ENDnext = find next place of END
                        }


                     if STARTnext < Y && Y-STARTnext >= SESSION_TIME
                         take (STARTnext, Y)










                       // check if the order of the conditions in the ifs is good

             */

            /*} else if (startOfCurrentSlot < insOfUserStudyStartTime.toEpochMilli()
                    && endOfCurrentSlot <= insOfUserStudyEndTime.toEpochMilli()) {
                // only trims the beginning of the slot
                // adds the slot to the list
                adjustedUserFreeSlots.add(new TimeSlot(new DateTime(insOfUserStudyStartTime.toEpochMilli()), new DateTime(endOfCurrentSlot)));
                totalFreeTime += (endOfCurrentSlot - insOfUserStudyStartTime.toEpochMilli()) / Constants.MILLIS_TO_HOUR;


            } else { // case 2 when slot starts at a day and ends at some other day

                // (e.g. instance of 22:00 at same day)
                insOfUserStudyEndTime = Instant.ofEpochMilli(startOfCurrentSlot);
                insOfUserStudyEndTime.with(ChronoField.HOUR_OF_DAY, userStudyEndTime);

                // (e.g. instance of 08:00 at the next day)
                insOfUserStudyStartTime = Instant.ofEpochMilli(endOfCurrentSlot);
                insOfUserStudyStartTime.with(ChronoField.HOUR_OF_DAY, userStudyStartTime);

                // finds the beginning of the first day
                long beginningOfFirstDay = Math.max(startOfCurrentSlot, insOfUserStudyStartTime.toEpochMilli());

                if ((insOfUserStudyEndTime.toEpochMilli() > beginningOfFirstDay)
                        && (((insOfUserStudyEndTime.toEpochMilli() - beginningOfFirstDay) / Constants.MILLIS_TO_HOUR) >= STUDY_SESSION_TIME)) {

                    adjustedUserFreeSlots.add(new TimeSlot(new DateTime(beginningOfFirstDay), new DateTime(insOfUserStudyEndTime.toEpochMilli())));
                    totalFreeTime += (insOfUserStudyEndTime.toEpochMilli() - beginningOfFirstDay) / Constants.MILLIS_TO_HOUR;

                }

                if (!Utility.isSameDay(insOfUserStudyStartTime, insOfUserStudyEndTime)) {
                    // add a day to the 22:00 instance
                    insOfUserStudyEndTime = insOfUserStudyEndTime.plus(1, ChronoUnit.DAYS);
                }

                // goes through full days and add the time slots
                // (e.g. 08:00 to 22:00)
                while (endOfCurrentSlot > insOfUserStudyEndTime.toEpochMilli()) {

                    // add a "full day" time slot
                    adjustedUserFreeSlots.add(new TimeSlot(new DateTime(insOfUserStudyStartTime.toEpochMilli()), new DateTime(insOfUserStudyEndTime.toEpochMilli())));
                    totalFreeTime += (insOfUserStudyEndTime.toEpochMilli() - insOfUserStudyStartTime.toEpochMilli()) / Constants.MILLIS_TO_HOUR;

                    // add a day to the 08:00 instance
                    insOfUserStudyStartTime = insOfUserStudyStartTime.plus(1, ChronoUnit.DAYS);
                    // add a day to the 22:00 instance
                    insOfUserStudyEndTime = insOfUserStudyEndTime.plus(1, ChronoUnit.DAYS);
                }

                // check for the last interval (e.g. 08:00-13:00)
                if (((endOfCurrentSlot - insOfUserStudyStartTime.toEpochMilli()) / Constants.MILLIS_TO_HOUR) >= STUDY_SESSION_TIME
                        && (insOfUserStudyStartTime.toEpochMilli() < endOfCurrentSlot)) {

                    adjustedUserFreeSlots.add(new TimeSlot(new DateTime(insOfUserStudyStartTime.toEpochMilli()), new DateTime(endOfCurrentSlot)));
                    totalFreeTime += (endOfCurrentSlot - insOfUserStudyStartTime.toEpochMilli()) / Constants.MILLIS_TO_HOUR;
                }*/
            /*}*/
        }

        return new DTOfreetime(adjustedUserFreeSlots, totalFreeTime);
    }

    public static String extractCourseFromExam(String summary) { // TO DO
        String courseName = "";

        // find course name from the string of the exam
        return courseName;
    }
}

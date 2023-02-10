package com.example.lamivhan.engine;

import com.example.lamivhan.model.timeslot.TimeSlot;
import com.example.lamivhan.model.user.User;
import com.example.lamivhan.utill.Constants;
import com.example.lamivhan.utill.dto.DTOfreetime;
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
        int STUDY_SESSION_TIME = user.getUserPreferences().getStudySessionTime();

        // goes through the raw time slots
        for (int i = 0; i < userFreeTimeSlots.size(); i++) {

            // gets current raw slot end and start in millis
            long startOfCurrentSlot = userFreeTimeSlots.get(i).getStart().getValue();
            long endOfCurrentSlot = userFreeTimeSlots.get(i).getEnd().getValue();


            // creates two "Instant"s that represent the day of the startOfCurrentSlot with different hours
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
                totalFreeTime += (endOfCurrentSlot - startOfCurrentSlot) / Constants.MILLIS_TO_HOUR;

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
                      v|---------START------X---------END----Y-----| else case
                      |---X-----START----------------END---Y--23:59----| // need to complete, now it adds the slot twice (thinks it's both the first interval and second interval)
                      v|---------START----------------END----------|

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



                       // check if the order of the conditions in the ifs is good

             */

            } else if (startOfCurrentSlot < insOfUserStudyStartTime.toEpochMilli()
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

                // add a day to the 22:00 instance
                insOfUserStudyEndTime = insOfUserStudyEndTime.plus(1, ChronoUnit.DAYS);

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
                }
            }
        }

        return new DTOfreetime(adjustedUserFreeSlots, totalFreeTime);
    }

    public static String extractCourseFromExam(String summary) {
        String courseName = "";

        // find course name from the string of the exam
        return courseName;
    }
}

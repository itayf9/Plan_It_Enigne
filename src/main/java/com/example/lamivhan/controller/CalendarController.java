package com.example.lamivhan.controller;

import com.example.lamivhan.engine.Engine;
import com.example.lamivhan.googleapis.AccessToken;
import com.example.lamivhan.model.course.CoursesRepository;
import com.example.lamivhan.model.user.User;
import com.example.lamivhan.model.user.UserRepository;
import com.example.lamivhan.utill.dto.DTOuserEvents;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@RestController
public class CalendarController {

    @Autowired
    private CoursesRepository courseRepo;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    @PostMapping(value = "/scan", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Event>> scanUserEvents(@RequestBody AccessToken accessToken, @RequestParam String email) throws IOException, GeneralSecurityException {

        // check if user exist in DB
        Optional<User> maybeUser = userRepo.findUserByEmail(email);
        if (maybeUser.isEmpty()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        DTOuserEvents userEvents = Engine.getEvents(accessToken,JSON_FACTORY,courseRepo);

        List<Event> fullDayEvents = userEvents.getFullDayEvents();

        // 1# get List of user's events
        List<Event> events = userEvents.getEvents();



        if (fullDayEvents.size() != 0) {
            // return list of events... for client to decide
        } else {
            // 2# 3# 4# 5#
            // generateStudyEvents(accessToken, true, events);
            // we can call the generateStudyEvents function, as usual, like it was some regular function
            // we need to pass all the required parameters
            // also, we need to check the ResponseEntity that is returned from the generateStudyEvents
            // then we can do things according to the HTTP status code, or body, from generateStudyEvents
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/generate", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Event>> generateStudyEvents(@RequestBody AccessToken accessToken, @RequestParam boolean scannedAlready) throws IOException, GeneralSecurityException {

        if (!scannedAlready) {

            // get user's calendar service
            Calendar calendarService = Engine.getCalendarService(accessToken, JSON_FACTORY, Constants.APPLICATION_NAME);

            // get user's calendar list

            List<CalendarListEntry> calendarList = Engine.getCalendarList(calendarService);

            // set up startDate & endDate
            // ...
            DateTime start = new DateTime(System.currentTimeMillis());
            DateTime end = new DateTime(System.currentTimeMillis() + Constants.ONE_MONTH_IN_MILLIS);

            List<Event> fullDayEvents = new ArrayList<>();
            List<Exam> examsFound = new LinkedList<>();

            // get List of user's events
            List<Event> events = Engine.getEventsFromALLCalendars(calendarService, calendarList, start, end, fullDayEvents, examsFound, courseRepo);

            if (fullDayEvents.size() != 0) {
                // return list of events... for client to decide
            } else {

                // get List of free time from list of events
                // decide which test gets each free time
                // create the events




                /*

                algorithm of #2
                1. Create Slot object  - done
                2. implement getFreeSlots(List<Event>); - in progress
                1. assume you have a list of Event (Google's Event) - done
                2. go through list, find free slots - in progress
                3. find total free time
                4. each slot is inserted to a list of free slots (slot is the gap between events)

                #4
                private List<StudySession> divideStudySessionsForExams(DTOfreetime dtoFreeTime, List<Course> exams);
                private Map<String, Double> getExamsRatio(List<Course> exams);
                User user = userRepo.findByEmail();
                breakValue = user.getPreferences().getUserBreakValue();

                int getSumOfRecommendedStudyTime(List<Course> exams);

                separate each slot in the free time list, to a few study sessions:
                4. determine on MINIMUM_STUDY_TIME, BREAK_DEFAULT
                5. find the proportions of each course from 100% study time. need to think how to do that.
                6. sum all recommended study time
                7. divide total free time / total recommended time
                8. take the max of MINIMUM_STUDY_TIME , the result of divide (7)

               embed the courses in the time slots:
                9. create events of courses depending on slots and proportions
                10. go through the free list and put the events




                 */
            Engine.generatePlanItCalendar(events, userEvents.getExamsFound(), maybeUser.get(), userEvents.getCalendarService());

        return new ResponseEntity<>(HttpStatus.OK);
    }
}

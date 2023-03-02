package com.example.lamivhan.controller;

import com.example.lamivhan.engine.CalendarEngine;
import com.example.lamivhan.engine.HolidaysEngine;
import com.example.lamivhan.model.mongo.course.CoursesRepository;
import com.example.lamivhan.model.mongo.user.User;
import com.example.lamivhan.model.mongo.user.UserRepository;
import com.example.lamivhan.utill.dto.DTOuserEvents;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.model.Event;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class CalendarController {

    @Autowired
    private Environment env;

    @Autowired
    private CoursesRepository courseRepo;

    @Autowired
    private UserRepository userRepo;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private Set<String> holidaysDatesCurrentYear;
    private Set<String> holidaysDatesNextYear;

    @PostConstruct
    private void init() {
        holidaysDatesCurrentYear = HolidaysEngine.getDatesOfHolidays(env.getProperty("holidays_api_key"), "il", Instant.now().get(ChronoField.YEAR));
        holidaysDatesNextYear = HolidaysEngine.getDatesOfHolidays(env.getProperty("holidays_api_key"), "il", Instant.now().get(ChronoField.YEAR) + 1);
    }


    /**
     * Scan the user's Calendar to get list of events and check to see if user has fullDayEvents existed.
     *
     * @param email user's email address to search the User on DB & get preferences.
     * @return ResponseEntity<List < Event>> we return list of events in a case of full day events found, otherwise we generate the calendar.
     * @throws IOException              IOException
     * @throws GeneralSecurityException GeneralSecurityException
     */
    @PostMapping(value = "/scan")
    public ResponseEntity<List<Event>> scanUserEvents(@RequestParam String email, @RequestParam String start, @RequestParam String end) throws IOException, GeneralSecurityException {

        // check if user exist in DB
        Optional<User> maybeUser = userRepo.findUserByEmail(email);
        if (maybeUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // get instance of the user
        User user = maybeUser.get();

        // check if accessToken is already invalid
        validateAccessToken(user);

        // 1# get List of user's events
        // perform a scan on the user's Calendar to get all of his events at the time interval
        DTOuserEvents userEvents = CalendarEngine.getEvents(user.getAccessToken(), start, end, JSON_FACTORY, courseRepo);

        // events - a list of events that represents all the user's events
        // fullDayEvents - a list of events that represents the user's full day events
        List<Event> fullDayEvents = userEvents.getFullDayEvents();
        List<Event> events = userEvents.getEvents();
        List<Event> copyOfFullDayEvents = new ArrayList<>(fullDayEvents);

        if (fullDayEvents.size() != 0) {

            // check if user want to study on holidays
            if (user.getUserPreferences().isStudyOnHolyDays()) {

                // scan through the list and check if an event is a holiday.
                for (Event fullDayEvent : fullDayEvents) {
                    if (holidaysDatesCurrentYear.contains(fullDayEvent.getStart().getDate().toStringRfc3339())
                            || holidaysDatesNextYear.contains(fullDayEvent.getStart().getDate().toStringRfc3339())) {

                        // remove the event from the copy of list of fullDayEvents and the events list
                        copyOfFullDayEvents.remove(fullDayEvent);
                        events.remove(fullDayEvent);
                    }
                }
            }

            // return the user with the updated list of fullDayEvents.
            return ResponseEntity.status(HttpStatus.CONFLICT).body(copyOfFullDayEvents);


        } else {

            CalendarEngine.generatePlanItCalendar(events, userEvents.getExamsFound(), maybeUser.get(), userEvents.getCalendarService(), userRepo);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ArrayList<>());
        }
    }

    /**
     * this endpoint re-scan the user Calendar events, deals with the full days events that has been found and generates the plan it calendar.
     *
     * @param email         user's email address to search the User on DB & get preferences
     * @param userDecisions array of boolean values representing
     * @return ResponseEntity<String> this method not suppose to fail unless it's been called externally
     * @throws IOException              IOException
     * @throws GeneralSecurityException GeneralSecurityException
     */
    @PostMapping(value = "/generate", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> generateStudyEvents(@RequestParam String email, @RequestParam String start, @RequestParam String end, @RequestBody boolean[] userDecisions) throws IOException, GeneralSecurityException {

        // check if user exist in DB
        Optional<User> maybeUser = userRepo.findUserByEmail(email);
        if (maybeUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // get instance of the user
        User user = maybeUser.get();

        // check if accessToken is already invalid
        validateAccessToken(user);

        // 1# get List of user's events
        // perform a scan on the user's Calendar to get all of his events at the time interval
        DTOuserEvents userEvents = CalendarEngine.getEvents(user.getAccessToken(), start, end, JSON_FACTORY, courseRepo);

        // events - a list of events that represents all the user's events
        // fullDayEvents - a list of events that represents the user's full day events
        List<Event> fullDayEvents = userEvents.getFullDayEvents();
        List<Event> events = userEvents.getEvents();

        // check if fullDayEvents List is empty (which doesn't suppose to be)
        if (fullDayEvents.size() != 0) {

            // go through the list
            for (int i = 0; i < fullDayEvents.size(); i++) {

                boolean userWantToStudyAtCurrentFullDayEvent = userDecisions[i];
                Event currentFullDayEvent = fullDayEvents.get(i);

                // check if user want to study at the current fullDayEvent
                if (userWantToStudyAtCurrentFullDayEvent) {
                    events.remove(currentFullDayEvent); // remove event element from the list of all events.
                }
            }
        }

        // 2# 3# 4# 5#
        CalendarEngine.generatePlanItCalendar(events, userEvents.getExamsFound(), maybeUser.get(), userEvents.getCalendarService(), userRepo);

        return ResponseEntity.status(HttpStatus.CREATED).body("PlanIt Calendar Has Been Created Successfully Good Luck !!");
    }

    /**
     * checks the access token of the user.
     * if not valid, refreshes the access token
     * also, updates the user's new access token in the DB
     *
     * @param user a {@link User} represents the user
     * @throws IOException IOException
     * @throws GeneralSecurityException GeneralSecurityException
     */
    private void validateAccessToken(User user) throws IOException, GeneralSecurityException {
        if (!CalendarEngine.isAccessTokenValid(user.getExpireTimeInMilliseconds())) {

            String clientID = env.getProperty("spring.security.oauth2.client.registration.google.client-id");
            String clientSecret = env.getProperty("spring.security.oauth2.client.registration.google.client-secret");

            // refresh the accessToken
            
            TokenResponse tokensResponse = CalendarEngine.refreshAccessToken(user.getRefreshToken(), clientID, clientSecret, JSON_FACTORY);
            long expireTimeInMilliseconds = Instant.now().plusMillis(((tokensResponse.getExpiresInSeconds() - 100) * 1000)).toEpochMilli();

            // updates the access token of the user in the DB
            user.setAccessToken(tokensResponse.getAccessToken());
            user.setExpireTimeInMilliseconds(expireTimeInMilliseconds);
            userRepo.save(user);
        }
    }
}

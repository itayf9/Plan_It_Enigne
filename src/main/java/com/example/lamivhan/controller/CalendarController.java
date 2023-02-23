package com.example.lamivhan.controller;

import com.example.lamivhan.engine.Engine;
import com.example.lamivhan.model.mongo.course.CoursesRepository;
import com.example.lamivhan.model.mongo.user.User;
import com.example.lamivhan.model.mongo.user.UserRepository;
import com.example.lamivhan.utill.dto.DTOuserEvents;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
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

    @PostMapping(value = "/scan")
    public ResponseEntity<List<Event>> scanUserEvents(@RequestParam String email) throws IOException, GeneralSecurityException {

        // check if user exist in DB
        Optional<User> maybeUser = userRepo.findUserByEmail(email);
        if (maybeUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User user = maybeUser.get();

        if (!Engine.isAccessTokenExpired(user.getAccessToken())) {

            String clientID = env.getProperty("spring.security.oauth2.client.registration.google.client-id");
            String clientSecret = env.getProperty("spring.security.oauth2.client.registration.google.client-secret");

            // refresh the accessToken
            Engine.refreshAccessToken(user.getRefreshToken(), clientID, clientSecret, JSON_FACTORY);

        }

        DTOuserEvents userEvents = Engine.getEvents(user.getAccessToken(), JSON_FACTORY, courseRepo);

        List<Event> fullDayEvents = userEvents.getFullDayEvents();

        // 1# get List of user's events
        List<Event> events = userEvents.getEvents();


        if (fullDayEvents.size() != 0) {
            // return list of events... for client to decide
        } else {

            Engine.generatePlanItCalendar(events, userEvents.getExamsFound(), maybeUser.get(), userEvents.getCalendarService(), userRepo);

            // also, we need to check the ResponseEntity that is returned from the generateStudyEvents
            // then we can do things according to the HTTP status code, or body, from generateStudyEvents
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/generate", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Event>> generateStudyEvents(@RequestParam String email) throws IOException, GeneralSecurityException {

        // check if user exist in DB
        Optional<User> maybeUser = userRepo.findUserByEmail(email);
        if (maybeUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // get instance of the user
        User user = maybeUser.get();

        // check if accessToken is still valid
        if (!Engine.isAccessTokenExpired(user.getAccessToken())) {

            String clientID = env.getProperty("spring.security.oauth2.client.registration.google.client-id");
            String clientSecret = env.getProperty("spring.security.oauth2.client.registration.google.client-secret");

            // refresh the accessToken
            Engine.refreshAccessToken(user.getRefreshToken(), clientID, clientSecret, JSON_FACTORY);

        }

        // 1# get List of user's events
        DTOuserEvents userEvents = Engine.getEvents(user.getAccessToken(), JSON_FACTORY, courseRepo);
        List<Event> fullDayEvents = userEvents.getFullDayEvents();
        List<Event> events = userEvents.getEvents();

        if (fullDayEvents.size() != 0) {

            // find the full day events that the user want to study at
            // exclude them from the list of events

        }

        // 2# 3# 4# 5#
        Engine.generatePlanItCalendar(events, userEvents.getExamsFound(), maybeUser.get(), userEvents.getCalendarService(), userRepo);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}

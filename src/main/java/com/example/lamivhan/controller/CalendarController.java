package com.example.lamivhan.controller;

import com.example.lamivhan.googleapis.AccessToken;
import com.example.lamivhan.model.course.Course;
import com.example.lamivhan.model.course.CoursesRepository;
import com.example.lamivhan.utill.Constants;
import com.example.lamivhan.utill.EventComparator;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class CalendarController {

    // check maybe syntax is not as it should be.
    @Autowired
    private CoursesRepository courseRepo;

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @PostMapping(value = "/login",
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public void login(@RequestBody AccessToken accessToken) {

        // 1. create Java user-object

        // 2. save user to DB with userRepo
    }

    /**
     *
     * @return a list of all courses in the DB
     */
    @GetMapping(value = "/courses")
    public List<Course> getCoursesNamesFromDB() {

        return courseRepo.findAll();
    }

    @GetMapping(value = "/profile", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public void getUserPreferencesFromDB(@RequestBody AccessToken accessToken) {

        // 1. User (Java Object) userPreferences / userProfile = userRepo.findByAccess_token??();
        // 2. return User
    }

    @GetMapping(value = "/logout")
    public void logout() {

        // 1. ???
    }

    @PostMapping(value = "/scan", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Event>> scanUserEvents(@RequestBody AccessToken accessToken, @RequestParam boolean scannedAlready) throws IOException, GeneralSecurityException {

        if (!scannedAlready) {
            // get user's calendar service
            Calendar calendarService = getCalendarService(accessToken);

            // get user's calendar list

            List<CalendarListEntry> calendarList = getCalendarList(calendarService);

            // set up startDate & endDate
            // ...
            DateTime start = new DateTime(System.currentTimeMillis());
            DateTime end = new DateTime(System.currentTimeMillis() + Constants.ONE_MONTH_IN_MILLIS);

            List<Event> fullDayEvents = new ArrayList<>();

            // get List of user's events
            List<Event> events = getEventsFromALLCalendars(calendarService, calendarList, start, end, fullDayEvents);


            if (fullDayEvents.size() != 0) {
                // return list of events... for client to decide
            } else {

                //createEvents(accessToken, true, events);
            }
        } else {
            // return full day events to client to get list of true-false in order to know which days he wants to study
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/generate", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Event>> generateStudyEvents(@RequestBody AccessToken accessToken, @RequestParam boolean scannedAlready) throws IOException, GeneralSecurityException {

        //createEvents()


        if (!scannedAlready) {

            // get user's calendar service
            Calendar calendarService = getCalendarService(accessToken);

            // get user's calendar list

            List<CalendarListEntry> calendarList = getCalendarList(calendarService);

            // set up startDate & endDate
            // ...
            DateTime start = new DateTime(System.currentTimeMillis());
            DateTime end = new DateTime(System.currentTimeMillis() + Constants.ONE_MONTH_IN_MILLIS);

            List<Event> fullDayEvents = new ArrayList<>();

            // get List of user's events
            List<Event> events = getEventsFromALLCalendars(calendarService, calendarList, start, end, fullDayEvents);

            if (fullDayEvents.size() != 0) {
                // return list of events... for client to decide
            } else {

                // get List of free time from list of events
                // decide which test gets each free time
                // create the events




                /*

                algorithm of #2
                1. assume you have a list of Event (Google's Event)
                2. go through list, find free slots
                3. find total free time
                4. each slot is inserted to a list of free slots (slot is the gap between events)


                #3

                // Create a new calendar
                 com.google.api.services.calendar.model.Calendar calendar = new Calendar();
                   calendar.setSummary("calendarSummary");
                    calendar.setTimeZone("America/Los_Angeles");

        // Insert the new calendar
                Calendar createdCalendar = service.calendars().insert(calendar).execute();


                #4

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


            }
        }

        // create events


        return new ResponseEntity<>(HttpStatus.OK);
    }

    private List<Event> getEventsFromALLCalendars(Calendar calendarService, List<CalendarListEntry> calendarList, DateTime start, DateTime end, List<Event> fullDayEvents) {
        List<Event> allEventsFromCalendars = new ArrayList<>();

        for (CalendarListEntry calendar : calendarList) {
            Events events = null;
            try {
                events = calendarService.events().list(calendar.getId())
                        .setTimeMin(start)
                        .setOrderBy("startTime")
                        .setTimeMax(end)
                        .setSingleEvents(true)
                        .execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            allEventsFromCalendars.addAll(events.getItems());
        }

        for (Event event : allEventsFromCalendars) {
            System.out.println(event.getStart().getDateTime() + " : " + event.getSummary());
        }
        allEventsFromCalendars.sort(new EventComparator());
        return allEventsFromCalendars;
    }

    private List<Event> getEventsFromCalendars(Calendar calendarService, List<CalendarListEntry> calendarList, DateTime start, DateTime end) {
        List<Event> allEventsFromCalendars = new ArrayList<>();

        for (CalendarListEntry calendar : calendarList) {
            Events events = null;
            try {
                events = calendarService.events().list(calendar.getId())
                        .setTimeMin(start)
                        .setOrderBy("startTime")
                        .setTimeMax(end)
                        .setSingleEvents(true)
                        .execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            allEventsFromCalendars.addAll(events.getItems());
        }


        return allEventsFromCalendars;
    }

    private List<CalendarListEntry> getCalendarList(Calendar calendarService) {
        String pageToken = null;
        List<CalendarListEntry> calendars = new ArrayList<>();
        do {
            CalendarList calendarList = null;
            try {
                calendarList = calendarService.calendarList().list().setPageToken(pageToken).execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            calendars.addAll(calendarList.getItems());

            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

        return calendars;
    }

    private Calendar getCalendarService(AccessToken access_token) throws GeneralSecurityException, IOException {

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Date expireDate = new Date(new Date().getTime() + 3600000);

        com.google.auth.oauth2.AccessToken accessToken = new com.google.auth.oauth2.AccessToken(access_token.getAccessToken(), expireDate);
        GoogleCredentials credential = new GoogleCredentials(accessToken);
        HttpRequestInitializer httpRequestInitializer = new HttpCredentialsAdapter(credential);

        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, httpRequestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();

    }


    public String test(AccessToken accessToken) throws GeneralSecurityException, IOException {

        // 1. get access_token from DB / request body / need to think about it...


        // 2. Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken.getAccessToken());
        Calendar service =
                new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        // Iterate through entries in calendar list
        String pageToken = null;
        do {
            CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> items = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : items) {
                System.out.println(calendarListEntry.getSummary() + " " + calendarListEntry.getId());
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("ggjkjd2dvspjiirkp5e9sv2566ujt1bh@import.calendar.google.com")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        StringBuilder tenEventsBuilder = new StringBuilder();

        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            FileWriter myWriter = new FileWriter("filename.txt");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                DateTime end = event.getEnd().getDateTime();
                if (end == null) {
                    end = event.getEnd().getDate();
                }

                System.out.printf("%s (%s) [%s]\n", event.getSummary(), start, end);
                myWriter.write("" + event.getSummary() + " (" + start + ") [" + end + "] \n");
                tenEventsBuilder.append(event.getSummary()).append(" (").append(start).append(") [").append(end).append("] \n");
            }
            myWriter.close();
        }

        return tenEventsBuilder.toString();
    }

    /**
     * creates the Plan-It calendar and adds it the user's calendar list
     * @param calendarService a calendar service of the user
     * @throws IOException in case of failure in "execute"
     */
    private void createPlanItCalendar(Calendar calendarService) throws IOException {

        // Create a new calendar
        com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
        calendar.setSummary("PlanIt Calendar");
        calendar.setTimeZone("Asia/Jerusalem");

        // Insert the new calendar
        com.google.api.services.calendar.model.Calendar createdCalendar = calendarService.calendars().insert(calendar).execute();
    }
}

package com.example.lamivhan.engine;

import com.example.lamivhan.googleapis.AccessToken;
import com.example.lamivhan.model.course.Course;
import com.example.lamivhan.model.course.CoursesRepository;
import com.example.lamivhan.model.exam.Exam;
import com.example.lamivhan.model.timeslot.TimeSlot;
import com.example.lamivhan.model.user.User;
import com.example.lamivhan.model.user.UserRepository;
import com.example.lamivhan.utill.Constants;
import com.example.lamivhan.utill.EventComparator;
import com.example.lamivhan.utill.Utility;
import com.example.lamivhan.utill.dto.DTOfreetime;
import com.example.lamivhan.utill.dto.DTOstartAndEndOfInterval;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Engine {

    private static CoursesRepository courseRepo;

    private static UserRepository userRepo;

    /**
     * Takes out all the free time slots that can be taken out of the user events.
     *
     * @param userEvents is an array with all the events the user had.
     * @param user       is containing user preferences.
     * @return DTOfreetime object the return from the function adjustFreeSlotsList.
     */
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

    /**
     * adjusts the free slots to the user's preferences
     *
     * @param userFreeTimeSlots a list of {@link TimeSlot} that contains the free time slots without user's preferences
     * @param user              a {@link User} that represents the user related to the slot list
     * @return a {@link DTOfreetime} that represents an adjusted free time slots
     */
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


        }

        return new DTOfreetime(adjustedUserFreeSlots, totalFreeTime);
    }

    public static String extractCourseFromExam(String summary) { // TO DO
        String courseName = "";

        // find course name from the string of the exam
        return courseName;
    }

    /**
     * @param calendarService Google Calendar service provider.
     * @param calendarList    List of all the User Google Calendars
     * @param start           the time to start scan of events
     * @param end             the time to end scan of events
     * @param fullDayEvents   list of full day events found
     * @return List of all the event's user has
     */
    public static List<Event> getEventsFromALLCalendars(Calendar calendarService, List<CalendarListEntry> calendarList, DateTime start, DateTime end, List<Event> fullDayEvents) {
        List<Event> allEventsFromCalendars = new ArrayList<>();
        List<Exam> examsFound = new LinkedList<>();

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
            // check if calendar is the exams calendar
            if (calendar.getSummary().equals("יומן אישי מתחנת המידע")) {
                // scan events to find exams
                for (Event event : events.getItems()) {
                    // check if event is an exam
                    if (event.getSummary().contains("מבחן")) {
                        // get exam/course name
                        String courseName = Engine.extractCourseFromExam(event.getSummary());
                        // query course from DB check if exist
                        Optional<Course> maybeFoundCourse = courseRepo.findCourseByCourseName(courseName);
                        // add to list of found exams
                        maybeFoundCourse.ifPresent(course -> examsFound.add(new Exam(course, event.getStart().getDateTime())));
                    }
                }
            }
            allEventsFromCalendars.addAll(events.getItems());
        }

        for (Event event : allEventsFromCalendars) {
            System.out.println(event.getStart().getDateTime() + " : " + event.getSummary());
        }
        allEventsFromCalendars.sort(new EventComparator());
        return allEventsFromCalendars;
    }

    // same function just without sort at the end - delete this
    private static List<Event> getEventsFromCalendars(Calendar calendarService, List<CalendarListEntry> calendarList, DateTime start, DateTime end) {
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

    /**
     * @param calendarService Google Calendar service provider.
     * @return List of all the User Google Calendars
     */
    public static List<CalendarListEntry> getCalendarList(Calendar calendarService) {
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

    /**
     * @param access_token     User Google AccessToken
     * @param JSON_FACTORY     Json Factory Instance
     * @param APPLICATION_NAME The name of the Application - PlanIt
     * @return Google Calendar service provider.
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static Calendar getCalendarService(AccessToken access_token, JsonFactory JSON_FACTORY, String APPLICATION_NAME) throws GeneralSecurityException, IOException {

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

    /**
     * get User from DB
     *
     * @param email unique email of username.
     * @return with optional<User> if user exist return the User Java object else will return null.
     */
    public static Optional<User> findUserByEmail(String email) {
        return userRepo.findUserByEmail(email);
    }

    public static void saveUserToDB(User user) {
        userRepo.save(user);
    }

    /*public String test(AccessToken accessToken) throws GeneralSecurityException, IOException {

        // 1. get access_token from DB / request body / need to think about it...


        // 2. Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken.getAccessToken());
        Calendar service =
                new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(Constants.APPLICATION_NAME)
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
    }*/

    /**
     * 3# creates the Plan-It calendar and adds it the user's calendar list
     *
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

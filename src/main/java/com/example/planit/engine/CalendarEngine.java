package com.example.planit.engine;

import com.example.planit.holidays.PlanITHolidays;
import com.example.planit.model.exam.Exam;
import com.example.planit.model.mongo.course.Course;
import com.example.planit.model.mongo.course.CoursesRepository;
import com.example.planit.model.mongo.user.StudyPlan;
import com.example.planit.model.mongo.user.User;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.model.studysession.StudySession;
import com.example.planit.model.timeslot.TimeSlot;
import com.example.planit.utill.Constants;
import com.example.planit.utill.EventComparator;
import com.example.planit.utill.Utility;
import com.example.planit.utill.dto.*;
import com.example.planit.utill.exception.UserCalendarNotFound;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.example.planit.utill.Constants.*;
import static com.example.planit.utill.Utility.roundInstantMinutesTime;

public class CalendarEngine {

    public static Logger logger = LogManager.getLogger(CalendarEngine.class);

    private final CoursesRepository courseRepo;

    private final UserRepository userRepo;

    private final String CLIENT_ID;
    private final String CLIENT_SECRET;

    private final PlanITHolidays holidays;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public CalendarEngine(String CLIENT_ID, String CLIENT_SECRET, UserRepository userRepo, CoursesRepository courseRepo,
                          PlanITHolidays holidays) {
        this.CLIENT_ID = CLIENT_ID;
        this.CLIENT_SECRET = CLIENT_SECRET;
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.holidays = holidays;
    }

    /**
     * Extract all the events that are in the user calendars.
     *
     * @return DTOuserEvents contains all the events, full day events and the exams
     * @throws GeneralSecurityException GeneralSecurityException
     * @throws IOException              IOException
     */
    public DTOuserCalendarsInformation getUserCalendarsInformation(User user, String start, String end) throws GeneralSecurityException, IOException {

        validateAccessTokenExpireTime(user);

        // get user's calendar service
        Calendar calendarService = getCalendarService(user.getAuth().getAccessToken(), user.getAuth().getExpireTimeInMilliseconds());

        // get user's calendar list
        List<CalendarListEntry> calendarList = getCalendarList(calendarService);

        List<Event> fullDayEvents = new ArrayList<>();
        List<Event> planItCalendarOldEvents = new ArrayList<>();
        List<Exam> examsFound = new LinkedList<>();
        // here we got the holidays date form user calendar "חגים בישראל"
        setHolidaysFromCalendar(calendarService, new DateTime(start), new DateTime(end));

        // validate token
        validateAccessTokenExpireTime(user);
        // get List of user's events
        List<Event> events = getEventsFromALLCalendars(calendarService, calendarList, new DateTime(start), new DateTime(end), fullDayEvents, planItCalendarOldEvents, examsFound, user.getPlanItCalendarID());
        return new DTOuserCalendarsInformation(fullDayEvents, planItCalendarOldEvents, examsFound, events, calendarService);
    }


    /**
     * @param allEvents list of the user events we found during the initial scan
     * @param exams     list of the user exams to determine when to stop embed free slots and division of study time.
     */
    public void generatePlanItCalendar(List<Event> allEvents, List<Exam> exams, User user, Calendar service, String start, List<Event> planItCalendarOldEvents, StudyPlan studyPlan)
            throws GeneralSecurityException {

        // gets the list of free slots
        DTOfreetime dtofreetime = getFreeSlots(allEvents, user, exams, start);

        // creates PlanIt calendar if not yet exists
        String planItCalendarID = createPlanItCalendar(service, user);

        // finds the proportions of each exam from 100% study time
        Map<Exam, Double> exam2Proportions = getExamsProportions(exams);

        // separates each slot in the free slots list, to a few study sessions and inserts breaks
        List<StudySession> sessionsList = separateSlotsToSessions(user, dtofreetime.getFreeTimeSlots());

        // calculates how many sessions belong to each course
        Map<Exam, Integer> exams2numberOfSessions = distributeNumberOfSessionsToCourses(exam2Proportions, sessionsList.size(), user);

        // goes from the end to the start and embed courses to sessions
        embedCoursesInSessions(exams2numberOfSessions, sessionsList, exams);

        // #5 - updates the planIt calendar
        updatePlanItCalendar(sessionsList, service, planItCalendarID, planItCalendarOldEvents, user, studyPlan);
    }

    /**
     * check if accessToken is still valid.
     * the function compares the expiration time with the current time.
     * the expiration time is related to an accessToken.
     *
     * @param expirationTime a long that represents the expiration time (in milliseconds)
     * @return true if the token is valid, false otherwise
     */
    public static boolean isAccessTokenValid(long expirationTime) {
        Instant expirationInstant = Instant.ofEpochMilli(expirationTime); // e.g. 1781874521 representing the time of 2023-05-17 - 14:30

        /* we add extra 5 minutes to make sure if token is about to be expired will be refreshed sooner */
        Instant now = Instant.now().plus(5, ChronoUnit.MINUTES);

        return expirationInstant.isAfter(now); // true if expire date is before the current time 2023-05-17 - 14:40
    }

    /**
     * get a new accessToken with the refresh token
     *
     * @param refreshToken the refreshToken
     * @param clientId     client id string
     * @param clientSecret client secret string
     * @return TokenResponse contains new accessToken
     * @throws IOException              IOException
     * @throws GeneralSecurityException GeneralSecurityException
     */
    public static TokenResponse refreshAccessToken(String refreshToken, String clientId, String clientSecret)
            throws IOException, GeneralSecurityException {

        // Create a RefreshTokenRequest to get a new access token using the refresh token
        RefreshTokenRequest refreshTokenRequest = new GoogleRefreshTokenRequest(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                refreshToken,
                clientId,
                clientSecret);

        refreshTokenRequest.setGrantType("refresh_token");

        // Execute the RefreshTokenRequest to get a new Credential object with the updated access token
        return refreshTokenRequest.execute();
    }

    /**
     * checks the access token of the user.
     * if not valid, refreshes the access token
     * also, updates the user's new access token in the DB
     *
     * @param user a {@link User} represents the user
     * @throws IOException              IOException
     * @throws GeneralSecurityException GeneralSecurityException
     */
    public void validateAccessTokenExpireTime(User user) throws IOException, GeneralSecurityException {

        // checks if the access token is not valid yet
        if (!CalendarEngine.isAccessTokenValid(user.getAuth().getExpireTimeInMilliseconds())) {

            // refresh the accessToken
            TokenResponse tokensResponse = CalendarEngine.refreshAccessToken(user.getAuth().getRefreshToken(), CLIENT_ID, CLIENT_SECRET);
            long expireTimeInMilliseconds = Instant.now().plusMillis(((tokensResponse.getExpiresInSeconds() - 100) * 1000)).toEpochMilli();

            // updates the access token of the user in the DB
            user.getAuth().setAccessToken(tokensResponse.getAccessToken());
            user.getAuth().setExpireTimeInMilliseconds(expireTimeInMilliseconds);
            userRepo.save(user);
        }

    }

    /**
     * get Google Calendar service provider.
     *
     * @param access_token User Google AccessToken
     * @return Google Calendar service provider.
     * @throws GeneralSecurityException GeneralSecurityException
     * @throws IOException              IOException
     */
    private static Calendar getCalendarService(String access_token, long expireTimeInMilliSeconds) throws GeneralSecurityException, IOException {

        //first check if scopes for "https://www.googleapis.com/auth/calendar" is still valid

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Date expireDate = new Date(expireTimeInMilliSeconds);

        AccessToken accessToken = new AccessToken(access_token, expireDate);
        GoogleCredentials credential = new GoogleCredentials(accessToken);
        HttpRequestInitializer httpRequestInitializer = new HttpCredentialsAdapter(credential);

        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, httpRequestInitializer)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build();
    }

    /**
     * get a List of all the User Google Calendars
     *
     * @param calendarService Google Calendar service provider.
     * @return List of all the User Google Calendars
     */
    private static List<CalendarListEntry> getCalendarList(Calendar calendarService) {
        String pageToken = null;
        List<CalendarListEntry> calendars = new ArrayList<>();
        do {
            CalendarList calendarList;
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
     * 1# get List of all the event's user has
     *
     * @param calendarService               Google Calendar service provider.
     * @param calendarList                  List of all the User Google Calendars
     * @param start                         the time to start scan of events
     * @param end                           the time to end scan of events
     * @param fullDayEventsFromAllCalendars list of full day events found
     * @return List of all the event's user has
     */
    private List<Event> getEventsFromALLCalendars(Calendar calendarService, List<CalendarListEntry> calendarList, DateTime start, DateTime end,
                                                  List<Event> fullDayEventsFromAllCalendars, List<Event> planItCalendarOldEvents, List<Exam> examsFound, String maybeExistingPlanItCalendarID) {
        List<Event> regularEventsFromAllCalendars = new ArrayList<>();
        boolean[] isEventBelongToMtaCalender = new boolean[1];
        boolean isMtaCalenderFound = false;

        List<Course> courses = courseRepo.findAll(); // get all courses from DB

        for (CalendarListEntry calendar : calendarList) {
            Events events;
            try {
                isEventBelongToMtaCalender[0] = false;
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
            if (calendar.getSummary().equals(Constants.EXAMS_CALENDAR_SUMMERY_NAME)) {
                isEventBelongToMtaCalender[0] = true;
                isMtaCalenderFound = true;
                // scan events to find exams
                for (Event event : events.getItems()) {
                    // check if event is an exam
                    if (event.getSummary().contains(Constants.EXAM_KEYWORD_TO_IDENTIFY_EXAMS)) {
                        // get exam/course name
                        Optional<Course> maybeFoundCourse = extractCourseFromExam(event.getSummary(), courses);

                        // add to list of found exams
                        maybeFoundCourse.ifPresent(course -> examsFound.add(new Exam(course, event.getStart().getDateTime())));
                    }

                }

            }

            // checks if calendar is the PlanIt calendar
            // ignores the PlanIt calendar in order to generate new study time slots
            if (calendar.getSummary().equals(PLANIT_CALENDAR_SUMMERY_NAME)
                    && maybeExistingPlanItCalendarID != null
                    && maybeExistingPlanItCalendarID.equals(calendar.getId())) {
                planItCalendarOldEvents.addAll(events.getItems());
                continue;
            }
/*
                    // if event in calendar college from 8:00 - 18:00
                    // we not need to add him.
*/
            // adds the events, excluding the full day events, from the calendar to the list
            regularEventsFromAllCalendars.addAll(events.getItems().stream().filter(event -> event.getStart().getDate() == null && !isEventAHolidayOfMTACalendar(event, isEventBelongToMtaCalender)).toList());
            // adds the full day events to the fullDayEvents list
            fullDayEventsFromAllCalendars.addAll(events.getItems().stream().filter(event -> event.getStart().getDate() != null).toList());
        }

        if (!isMtaCalenderFound) {
            throw new UserCalendarNotFound(Constants.COLLEGE_CALENDAR_NOT_FOUND);
        }

        // sorts the events, so they will be ordered by start time
        regularEventsFromAllCalendars.sort(new EventComparator());
        fullDayEventsFromAllCalendars.sort(new EventComparator());
        return regularEventsFromAllCalendars;
    }

    /***
     * check if the current is from Mta Calendar, the time of the event is start at 8 a.m and end at 18 p.m.
     * @param event the current event to check
     * @param isEventBelongToMtaCalender have the answer if the current event is from the Mta calendar
     * @return result if the event is from the Mta calendar and start at 8 a.m and end at 18 p.m
     */
    private boolean isEventAHolidayOfMTACalendar(Event event, boolean[] isEventBelongToMtaCalender) {
        boolean result = false;
        if (isEventBelongToMtaCalender[0]) {
            // create instant for the start time and end time of the current event
            Instant startOfCurrentEvent = Instant.ofEpochMilli(event.getStart().getDateTime().getValue());
            Instant endOfCurrentEvent = Instant.ofEpochMilli(event.getEnd().getDateTime().getValue());

            // get start time from the event
            int startOfCurrentSlotHour = startOfCurrentEvent.atZone(ZoneId.of(Constants.ISRAEL_TIME_ZONE)).getHour();
            // get end time from the event
            int endOfCurrentSlotHour = endOfCurrentEvent.atZone(ZoneId.of(Constants.ISRAEL_TIME_ZONE)).getHour();

            if (startOfCurrentSlotHour == 8 && endOfCurrentSlotHour == 18) {
                // check if the start (hours) = 8 && end (hours) = 18
                result = true;
            }
        }

        return result;
    }

    /**
     * find the name of the course, from the String that contains the event summery of an exam event.
     * e.g מבחן מועד 1  ציון בחינה - פרונטלי גב' אריאן שלומית חישוביות
     * return "חישוביות"
     */
    private static Optional<Course> extractCourseFromExam(String summary, List<Course> courses) { // TO DO
        String[] courseName = {""}; // init empty array

        // find course name from the string of the exam
        String[] summeryInWords = summary.split(" ");
        Optional<Course> maybeFoundCourse = Optional.empty();

        // scan through the String array to add words that finally will add up to a course name from the DB
        for (int i = summeryInWords.length - 1; i >= 0; i--) {

            // assign the new word to the start of the current course-name concatenation
            courseName[0] = (summeryInWords[i] + " " + courseName[0]).trim();

            // try to get a Course from the list of courses in the DB
            maybeFoundCourse = courses.stream().filter(Course -> Course.getCourseName().equals(courseName[0])).findFirst();

            // check if found course is not a null
            if (maybeFoundCourse.isPresent()) {
                break;
            }
        }

        return maybeFoundCourse;
    }

    /**
     * 2# Takes out all the free time slots that can be taken out of the user events.
     *
     * @param userEvents is an array with all the events the user had.
     * @param user       is containing user preferences.
     * @return DTOfreetime object the return from the function adjustFreeSlotsList.
     */
    private static DTOfreetime getFreeSlots(List<Event> userEvents, User user, List<Exam> examsFound, String start) {

        Exam lastExam = examsFound.get(examsFound.size() - 1);
        long startTimeOfLastExam = lastExam.getDateTime().getValue();
        List<TimeSlot> userFreeTimeSlots = new ArrayList<>();

        // get the free slot before the first event
        if (userEvents.size() > 0) {
            long startOfFirstEvent = userEvents.get(0).getStart().getDateTime().getValue();
            userFreeTimeSlots.add(new TimeSlot(new DateTime(start), new DateTime(startOfFirstEvent)));
        }

        // get all free time slots in the event
        for (int i = 0; i < userEvents.size() - 1; i++) {
            // get start of event in (i+1) place
            long startOfNextEvent = userEvents.get(i + 1).getStart().getDateTime().getValue();
            // get the end of event in (i) place
            long endOfCurrentEvent = userEvents.get(i).getEnd().getDateTime().getValue();
            if (endOfCurrentEvent < startOfNextEvent) {
                //check if we have time to add, add to list of free time.
                userFreeTimeSlots.add(new TimeSlot(new DateTime(endOfCurrentEvent), new DateTime(startOfNextEvent)));
                if (startTimeOfLastExam == startOfNextEvent) {
                    break;
                }
            }
        }

        DTOfreetime dtoFreeSlotsAfterAdjust = adjustFreeSlotsList(userFreeTimeSlots, user);
        boolean isUserStudyInWeekend = user.getUserPreferences().isStudyOnWeekends();

        if (!isUserStudyInWeekend) {
            dtoFreeSlotsAfterAdjust = adjustFreeSlotListByWeekend(dtoFreeSlotsAfterAdjust);
        }

        return dtoFreeSlotsAfterAdjust;
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

            if (selectedStart.until(selectedEnd, ChronoUnit.MINUTES) >= userStudySessionTime) {
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
                userStudyStartNext = Utility.addDayToCurrentInstant(userStudyStartNext);
                userStudyEndNext = Utility.addDayToCurrentInstant(userStudyEndNext);
            }

            if ((userStudyStartNext.isBefore(endOfCurrentSlot))
                    && (userStudyStartNext.until(endOfCurrentSlot, ChronoUnit.MINUTES) >= userStudySessionTime)) {
                // adds the study time of the last day
                adjustedUserFreeSlots.add(new TimeSlot(new DateTime(userStudyStartNext.toEpochMilli()), new DateTime(endOfCurrentSlot.toEpochMilli())));
                totalFreeTime += (endOfCurrentSlot.toEpochMilli() - userStudyStartNext.toEpochMilli()) / Constants.MILLIS_TO_HOUR;
            }
        }
        return new DTOfreetime(adjustedUserFreeSlots, totalFreeTime);
    }

    /**
     * adjust the list and total time in the DTOfreetime without the weekend in the list of the free time.
     *
     * @param dtoFreeSlotsAfterAdjust provide the free time slots and the total time with the weekend include.
     * @return DTOfreetime with a new list of free slots and total time without the weekend
     */
    private static DTOfreetime adjustFreeSlotListByWeekend(DTOfreetime dtoFreeSlotsAfterAdjust) {
        List<TimeSlot> freeSlotList = dtoFreeSlotsAfterAdjust.getFreeTimeSlots();
        List<TimeSlot> freeSlotListAfterAdjust = new ArrayList<>();
        int updateTotalTime = 0;

        for (TimeSlot userFreeTimeSlot : freeSlotList) {
            // gets current slot end and start
            Instant startOfCurrentSlot = Instant.ofEpochMilli(userFreeTimeSlot.getStart().getValue());
            Instant endOfCurrentSlot = Instant.ofEpochMilli(userFreeTimeSlot.getEnd().getValue());
            // get the day of the week in the instant start
            int startOfCurrentSlotDayInWeek = startOfCurrentSlot.atZone(ZoneId.of(Constants.ISRAEL_TIME_ZONE)).getDayOfWeek().getValue();
            int startOfCurrentSlotHour = startOfCurrentSlot.atZone(ZoneId.of(Constants.ISRAEL_TIME_ZONE)).getHour();
            // get the day of the week in the instant end
            int endOfCurrentSlotHour = endOfCurrentSlot.atZone(ZoneId.of(Constants.ISRAEL_TIME_ZONE)).getHour();

            // the day is Friday (start - 16:00)
            if (startOfCurrentSlotDayInWeek == 5) {
                if (startOfCurrentSlotHour < 16) { // start before 16:00
                    if (endOfCurrentSlotHour <= 16) { // end before 16:00
                        freeSlotListAfterAdjust.add(userFreeTimeSlot);
                    } else { // end after 16:00
                        // update the userFreeTimeSlot end time to 16:00

                        endOfCurrentSlot = endOfCurrentSlot
                                .atZone(ZoneId.of(Constants.ISRAEL_TIME_ZONE))
                                .withHour(16)
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0)
                                .toInstant();

                        // add to freeSlotListAfterAdjust
                        freeSlotListAfterAdjust.add(new TimeSlot(new DateTime(startOfCurrentSlot.toEpochMilli()), new DateTime(endOfCurrentSlot.toEpochMilli())));
                        // add to updateTotalTime the update free time
                    }
                    updateTotalTime += (endOfCurrentSlot.toEpochMilli() - startOfCurrentSlot.toEpochMilli()) / Constants.MILLIS_TO_HOUR;
                }
                // else throw start and end in the weekend
            }
            // the day is Saturday (20:00 - end)
            else if (startOfCurrentSlotDayInWeek == 6) {
                if (startOfCurrentSlotHour < 20) { // start after 20:00
                    if (endOfCurrentSlotHour >= 20) {
                        // update the userFreeTimeSlot start time to 20:00
                        startOfCurrentSlot = startOfCurrentSlot
                                .atZone(ZoneId.of(Constants.ISRAEL_TIME_ZONE))
                                .withHour(20)
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0)
                                .toInstant();
                        // add to freeSlotListAfterAdjust
                        freeSlotListAfterAdjust.add(new TimeSlot(new DateTime(startOfCurrentSlot.toEpochMilli()), new DateTime(endOfCurrentSlot.toEpochMilli())));
                        // add to updateTotalTime the update free time
                        updateTotalTime += (endOfCurrentSlot.toEpochMilli() - startOfCurrentSlot.toEpochMilli()) / Constants.MILLIS_TO_HOUR;
                    }
                    // else throw start and end in the weekend
                } else {
                    freeSlotListAfterAdjust.add(userFreeTimeSlot);
                    updateTotalTime += (endOfCurrentSlot.toEpochMilli() - startOfCurrentSlot.toEpochMilli()) / Constants.MILLIS_TO_HOUR;
                }
            } else {
                freeSlotListAfterAdjust.add(userFreeTimeSlot);
                updateTotalTime += (endOfCurrentSlot.toEpochMilli() - startOfCurrentSlot.toEpochMilli()) / Constants.MILLIS_TO_HOUR;
            }

        }
        return new DTOfreetime(freeSlotListAfterAdjust, updateTotalTime);
    }

    /**
     * 3# creates the Plan-It calendar and adds it the user's calendar list
     *
     * @param calendarService a calendar service of the user
     */
    private String createPlanItCalendar(Calendar calendarService, User user) {

        String planItCalendarID;
        String planItCalendarIdFromDB = user.getPlanItCalendarID();

        // checks if the calendar already exists in DB
        try {
            validateAccessTokenExpireTime(user);
            if (planItCalendarIdFromDB != null && calendarService.calendars().get(planItCalendarIdFromDB).execute() != null) {
                return planItCalendarIdFromDB;
            }
        } catch (IOException | GeneralSecurityException ignored) {
            // if we end up in here, then calendar was deleted by the user...
        }

        // else {
        // Create a new calendar
        com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
        calendar.setSummary(PLANIT_CALENDAR_SUMMERY_NAME);
        calendar.setTimeZone(ISRAEL_TIME_ZONE);

        // Insert the new calendar
        com.google.api.services.calendar.model.Calendar createdCalendar;
        try {
            validateAccessTokenExpireTime(user);
            createdCalendar = calendarService.calendars().insert(calendar).execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        planItCalendarID = createdCalendar.getId();
        user.setPlanItCalendarID(planItCalendarID);
        userRepo.save(user);

        return planItCalendarID;
    }

    /**
     * calculates the proportions of the courses that the user has.
     * for each course, the proportion is determined by a double (e.g. 0.995 is 99.5%)
     *
     * @param exams a list of {@link Exam} that represents the exams that the user have
     * @return a map of string to double that represents the course names and their proportion
     */
    private static Map<Exam, Double> getExamsProportions(List<Exam> exams) {

        Map<Exam, Integer> exam2TotalValue = new HashMap<>();
        int sumTotalValues = 0;
        Map<Exam, Double> exam2Proportion = new HashMap<>();

        for (Exam exam : exams) {

            Course currentCourse = exam.getCourse();

            // gets the total value of the course ( credits + difficulty level + recommended study time)
            int currentCourseTotalValue = currentCourse.getCredits() + currentCourse.getDifficultyLevel() + currentCourse.getRecommendedStudyTime();

            // adds the exam to the map of total values
            exam2TotalValue.put(exam, currentCourseTotalValue);
            sumTotalValues += currentCourseTotalValue;
        }

        for (Map.Entry<Exam, Integer> examTotalValueMapEntry : exam2TotalValue.entrySet()) {
            Exam currentExam = examTotalValueMapEntry.getKey();
            int currentExamTotalValue = examTotalValueMapEntry.getValue();

            // calculates the percentage of the exam's value
            double currentExamProportion = ((double) currentExamTotalValue / (double) sumTotalValues);

            // adds the exam to the map of proportions
            exam2Proportion.put(currentExam, currentExamProportion);
        }

        return exam2Proportion;
    }

    /**
     * separate the free time slots to study sessions.
     *
     * @param user          use for get user preferences.
     * @param freeTimeSlots have all the free time slots.
     * @return list of study session
     */
    private static List<StudySession> separateSlotsToSessions(User user, List<TimeSlot> freeTimeSlots) {

        List<StudySession> listOfStudySessions = new ArrayList<>();
        long breakTime = user.getUserPreferences().getUserBreakTime();
        int studyTimeInMinutes = user.getUserPreferences().getStudySessionTime();

        // go through the slots list
        for (TimeSlot timeSlot : freeTimeSlots) {
            // initial the startOfSession and endOfSession.
            // the sessions are rounded to 15 minutes intervals
            Instant startOfSession = roundInstantMinutesTime(Instant.ofEpochMilli(timeSlot.getStart().getValue()), true);
            Instant endOfSession = startOfSession.plus(studyTimeInMinutes, ChronoUnit.MINUTES);

            // while "endOfSession" is in the range of the slot
            while (endOfSession.toEpochMilli() <= timeSlot.getEnd().getValue()) {
                // add the current study session to the list
                listOfStudySessions.add(new StudySession(new DateTime(startOfSession.toEpochMilli()), new DateTime(endOfSession.toEpochMilli())));
                // add to the endOfSession the break time
                endOfSession = endOfSession.plus(breakTime, ChronoUnit.MINUTES);
                // initial the new startOfSession and endOfSession.
                startOfSession = endOfSession;
                endOfSession = startOfSession.plus(studyTimeInMinutes, ChronoUnit.MINUTES);
            }

            // if the "startOfSession" is in the range and the "endOfSession" is out of range,
            // adds the session from "startOfSession" to the end of range.
            if (startOfSession.toEpochMilli() < timeSlot.getEnd().getValue()
                    && timeSlot.getEnd().getValue() - startOfSession.toEpochMilli() >= (long) studyTimeInMinutes * MINUTES_TO_MILLIS) {
                Instant endOfSlot = roundInstantMinutesTime(Instant.ofEpochMilli(timeSlot.getEnd().getValue()), false);
                listOfStudySessions.add(new StudySession(new DateTime(startOfSession.toEpochMilli()), new DateTime(endOfSlot.toEpochMilli())));
            }
        }

        return listOfStudySessions;
    }

    /**
     * for every session, calculates the number of sessions, considering the proportions and the number of total sessions.
     * for each course X, the number of sessions is calculated as a rounded value of: (number of sessions * proportions of course X)
     *
     * @param exam2Proportions a map that contains values of proportions for each exam
     * @param numOfSessions    the number of sessions available
     * @return a map of string to int that represents, for each course name, the number of sessions
     */
    private static Map<Exam, Integer> distributeNumberOfSessionsToCourses(Map<Exam, Double> exam2Proportions, int numOfSessions, User user) {
        Map<Exam, Integer> exams2numberOfSessions = new HashMap<>();

        // goes through the exams' proportions and calculates the number of sessions
        for (Map.Entry<Exam, Double> examProportionMapEntry : exam2Proportions.entrySet()) {
            Exam exam = examProportionMapEntry.getKey();
            Double proportion = examProportionMapEntry.getValue();

            // gets the number of sessions for the current exam
            Integer numberOfSessionForCurrentExam = (int) Math.round(numOfSessions * proportion);

            // adds the number of sessions to the map
            exams2numberOfSessions.put(exam, numberOfSessionForCurrentExam);
        }

        // adjusts the number of sessions to the recommended value
        adjustTheNumberOfSessionsToRecommendedValue(exams2numberOfSessions, user);

        return exams2numberOfSessions;

    }

    /**
     * adjusts for each exam, the number of sessions needed.
     * considers the recommended study time for each exam, selects  the min( calculated number of sessions, recommended study time)
     * e.g. let A be a course with the following parameters: exam: X, recommendedStudyTime: 10
     * let exams2numberOfSessions.get(X) be 40
     * the adjusted study time is 10
     *
     * @param exams2numberOfSessions the map that contains {@link Exam} as keys and Integer representing the number of sessions to generate per exam
     */
    private static void adjustTheNumberOfSessionsToRecommendedValue(Map<Exam, Integer> exams2numberOfSessions, User user) {

        int userStudySessionTime = user.getUserPreferences().getStudySessionTime();

        int userStudyIntervalInMinutes = Utility.getTotalMinutesOfStudyInDay(user.getUserPreferences().getUserStudyStartTime(), user.getUserPreferences().getUserStudyEndTime());

        // goes through the exams2numberOfSessions map and updates the number of sessions
        for (Map.Entry<Exam, Integer> entry : exams2numberOfSessions.entrySet()) {

            int recommendedStudyTimeOfCourseInTheMap = entry.getKey().getCourse().getRecommendedStudyTime();
            int oldNumberOfSessions = entry.getValue();

            int totalMinutesUserIsRecommendedToStudy = recommendedStudyTimeOfCourseInTheMap * userStudyIntervalInMinutes;

            int newNumberOfSessions = totalMinutesUserIsRecommendedToStudy / userStudySessionTime;

            newNumberOfSessions = Math.min(newNumberOfSessions, oldNumberOfSessions);

            exams2numberOfSessions.put(entry.getKey(), newNumberOfSessions);
        }
    }

    /**
     * embeds the details (courses' names and subjects) in the user's sessions list.
     * embeds the courses considering exams' dates, courses' proportions, and information about courses subjects
     *
     * @param exams2numberOfSessions a map of string to int that represents, for each course name, the required number of sessions
     * @param sessionsList           a list of {@link StudySession} that represents the user's created study sessions
     * @param exams                  a list of {@link Exam} that represents the user's exams
     */
    private static void embedCoursesInSessions(Map<Exam, Integer> exams2numberOfSessions, List<StudySession> sessionsList, List<Exam> exams) {

        // represents each exam with a unique index identifier.
        Map<Exam, Integer> exam2Index = new HashMap<>();
        for (int i = 0; i < exams.size(); i++) {
            exam2Index.put(exams.get(i), i);
        }

        // embeds the courses' names in the sessions list
        embedCoursesNamesInSessions(exams2numberOfSessions, sessionsList, exams);
        // embeds the courses' subjects in the sessions list.
        embedCoursesSubjectsInSessions(exam2Index, sessionsList, exams);
    }

    /**
     * embeds the courses names in the sessions, considering the exams dates and the number of sessions required for each course.
     *
     * @param exams2numberOfSessions a map of string to int that represents, for each course name, the required number of sessions
     * @param sessionsList           a list of {@link StudySession} that represents the user's created study sessions
     * @param exams                  a list of {@link Exam} that represents the user's exams
     */
    private static void embedCoursesNamesInSessions(Map<Exam, Integer> exams2numberOfSessions, List<StudySession> sessionsList, List<Exam> exams) {

        Stack<Exam> nextExams = new Stack<>();
        int currentExamIndex = exams.size() - 1;

        // initiate the set with the last dated exam in the exams period
        nextExams.push(exams.get(currentExamIndex));
        currentExamIndex--;

        // goes through the sessions from the end to the start
        for (int i = sessionsList.size() - 1; i >= 0; i--) {

            // if the current session starts before the following exam to be seen
            // e.g. if 08:00-10:00 of 09/07 is before the 10/07
            if (currentExamIndex > -1 && sessionsList.get(i).getStart().getValue() < exams.get(currentExamIndex).getDateTime().getValue()) {
                nextExams.push(exams.get(currentExamIndex));
                currentExamIndex--;
            }

            /* checks if the nextExams stack is empty.
               if empty, removes the next sessions from the sessionsList,
               until we reach the next exam */
            if (nextExams.isEmpty()) {
                sessionsList.remove(i);
                continue;
            }

            // sets the session to be associated with the exam that is the closest to the session
            sessionsList.get(i).setCourseName(nextExams.peek().getCourse().getCourseName());
            sessionsList.get(i).setExamToStudyFor(nextExams.peek());

            // extract course name and sessions-count values
            Exam exam = nextExams.peek();
            int numOfSessionsPerExam = exams2numberOfSessions.get(nextExams.peek());

            // update session count value and save new value to the map
            numOfSessionsPerExam -= 1;

            if (numOfSessionsPerExam != 0) { // if there are more session left to insert, updates the value in the map
                exams2numberOfSessions.put(exam, numOfSessionsPerExam);
            } else { // if no more sessions left to insert, removes exam from stack
                nextExams.pop();
            }
        }
    }

    /**
     * embeds the courses subjects in the sessions, assuming all the sessions have been embedded with courses names.
     *
     * @param exams2IndexInListOfExams a map of string to int, representing for each course name, a unique index that is helpful for identifying the course in an internal array
     * @param sessionsList             a list of {@link StudySession} that represent the user's created study sessions
     * @param exams                    a list of a {@link Exam} that represents the user's exams
     */
    private static void embedCoursesSubjectsInSessions(Map<Exam, Integer> exams2IndexInListOfExams, List<StudySession> sessionsList, List<Exam> exams) {

        // create list of a list of study-sessions to make insertion of subjects easier later.
        List<List<StudySession>> listOfListOfStudySessions = new ArrayList<>();
        for (Exam ignored : exams) {
            listOfListOfStudySessions.add(new ArrayList<>());
        }

        // run through the sessions list and initialize the list of lists
        for (StudySession session : sessionsList) {

            int examIndexInListOfLists = exams2IndexInListOfExams.get(session.getExamToStudyFor());
            listOfListOfStudySessions.get(examIndexInListOfLists).add(session);
        }


        //  run through the list of lists and embed the subjects, for each session
        for (int i = 0; i < exams.size(); i++) {

            Exam currentExam = exams.get(i);

            String[] subjects = currentExam.getCourse().getCourseSubjects(); // e.g ["עוצמות" ,"פונקציות"]
            int numberOfSubjectsInCurrentExam = subjects.length;
            int indexOfCurrentSubject = 0;

            // presents the percentage for study the subject for the current exam.
            double subjectsToExamsPracticeProportions = (currentExam.getCourse().getSubjectsPracticePercentage()) * 0.01; // e.g - 60% of 100%

            List<StudySession> sessionsListOfCurrentExam = listOfListOfStudySessions.get(i);
            int numberOfSessions = sessionsListOfCurrentExam.size();
            int numberOfSessionsForSubjects = (int) Math.ceil(numberOfSessions * subjectsToExamsPracticeProportions);

            double subjectsPerSession = (double) numberOfSubjectsInCurrentExam / (double) numberOfSessionsForSubjects;
            double subjectsPerSessionCounter = 0;
            int nextSubjectsPerSessionInteger = 1;

            // makes the subjectsPerSession an integer
            if (subjectsPerSession >= 1) {
                subjectsPerSession = Math.ceil(subjectsPerSession);
            }


            for (int j = 0; j < numberOfSessions; j++) {

                if (numberOfSessionsForSubjects <= j) {
                    // set "test" Description in the current session
                    sessionsListOfCurrentExam.get(j).setDescription(EVENT_DESCRIPTION_PRACTISE_PREV_EXAMS);
                } else {

                    if (subjectsPerSession < 1) {
                        // numOfSubjects < numOfSessions
                        // each subject get more than one session.
                        sessionsListOfCurrentExam.get(j).setDescription(subjects[indexOfCurrentSubject]);
                        subjectsPerSessionCounter += subjectsPerSession;
                        // we pass to the need subject.
                        if ((int) subjectsPerSessionCounter == nextSubjectsPerSessionInteger) {
                            indexOfCurrentSubject++;
                            nextSubjectsPerSessionInteger++;
                        }


                    } else if (subjectsPerSession == 1) {
                        // numOfSubjects = numOfSessions
                        // each subject gets one session
                        sessionsListOfCurrentExam.get(j).setDescription(subjects[indexOfCurrentSubject]);
                        indexOfCurrentSubject++;

                    } else if (subjectsPerSession > 1) {
                        // numOfSubjects > numOfSessions
                        // each session get more than one subject.
                        int k;
                        StringBuilder subjectsToStudyBuilder = new StringBuilder();

                        // inserts the next few subjects in the new session
                        for (k = 0; k < subjectsPerSession && indexOfCurrentSubject + k < subjects.length; k++) {
                            if (k != 0) {
                                subjectsToStudyBuilder.append(" , ");
                            }
                            subjectsToStudyBuilder.append(subjects[indexOfCurrentSubject + k]);
                        }
                        sessionsListOfCurrentExam.get(j).setDescription(subjectsToStudyBuilder.toString());
                        indexOfCurrentSubject += k;
                    }
                }


                /*

                sessions      subjects            Math.ceil( subjects/sessions )        ordering
                  3              5                             2                        2, 2, 1
                  2              10                            5                        5, 5
                  4              15                            4                        4, 4, 4, 3
                  5              5                             1                        1,1,1,1,1
                  10             2                             0.2                      0.5, 0.5 0.5, 0.5 0.5, 0.5 0.5, 0.5 0.5, 0.5

                 */


            }
        }
    }

    /**
     * updates the PlanIt calendar with the new sessions list.
     * clears the calendar and create new Google {@link Event} for each study session
     *
     * @param sessionsList     a list of {@link StudySession} that represents the user's study sessions
     * @param service          the Google's {@link Calendar} service
     * @param planItCalendarID the calendar ID of the PlanIt calendar in the user's calendar list
     */
    private void updatePlanItCalendar(List<StudySession> sessionsList, Calendar service, String planItCalendarID, List<Event> planItCalendarOldEvents, User user, StudyPlan studyPlan) throws GeneralSecurityException {
        studyPlan.setTotalNumberOfStudySessions(sessionsList.size());

        List<Event> overlapsOldEvents = getOverlapOldEventsPlanItCalendar(sessionsList, planItCalendarOldEvents);

        for (Event eventToBeDeleted : overlapsOldEvents) {
            try {
                validateAccessTokenExpireTime(user);
                // removes overlap events in the PlanIt calendar
                service.events().delete(planItCalendarID, eventToBeDeleted.getId()).execute();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //studyPlan.setTotalNumberOfStudySessions(sessionsList.size());
        // add updated events to the PlanIt calendar
        // this loop goes through the sessions and adds them to the PlanIt calendar
        for (StudySession session : sessionsList) {

            // creates a new Google Event
            Event event = new Event()
                    .setSummary(Constants.EVENT_SUMMERY_PREFIX + session.getCourseName())
                    .setDescription(session.getDescription())
                    .setStart(new EventDateTime()
                            .setDateTime(session.getStart())
                            .setTimeZone(ISRAEL_TIME_ZONE))
                    .setEnd(new EventDateTime()
                            .setDateTime(session.getEnd())
                            .setTimeZone(ISRAEL_TIME_ZONE));

            // inserts the new Google Event to the PlanIt calendar
            try {
                validateAccessTokenExpireTime(user);
                service.events().insert(planItCalendarID, event).execute();

            } catch (GoogleJsonResponseException e) {
                System.out.println(e.getDetails());
                System.out.println(e.getStatusCode());
                System.out.println(e.getMessage());
                logger.error(MessageFormat.format("planitcalender id:{0}, google message:{1}", planItCalendarID, e.getMessage()));
                throw new RuntimeException();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * finds all the overlapping events from the old PlanIt calendar (from previous generating processes).
     * the new generated sessions are compared to the old generated events.
     * the overlapping events are removed from sessionsList.
     *
     * @param sessionsList            the new list of {@link StudySession} that about to be created as an events
     * @param planItCalendarOldEvents the old list of {@link Event} that been created and to be checked if causing an overlap anywhere
     * @return list of old {@link Event} from the PlanIt calendar that will later be deleted from the calendar.
     */
    private static List<Event> getOverlapOldEventsPlanItCalendar(List<StudySession> sessionsList, List<Event> planItCalendarOldEvents) {
        List<Event> overlappingEvents = new ArrayList<>();
        List<Integer> newSessionsIndicesToBeRemoved = new ArrayList<>();

        int newSessionIndex = 0;
        int oldEventIndex = 0;

        while (newSessionIndex < sessionsList.size() && oldEventIndex < planItCalendarOldEvents.size()) {
            StudySession newSession = sessionsList.get(newSessionIndex);
            Event oldEvent = planItCalendarOldEvents.get(oldEventIndex);

            if (newSession.getEnd().getValue() < (oldEvent.getStart().getDateTime().getValue())) {
                // new event ends before old event starts, move to next new event
                newSessionIndex++;
            } else if (newSession.getStart().getValue() > (oldEvent.getEnd().getDateTime().getValue())) {
                // new event starts after old event ends, move to next old event
                oldEventIndex++;
            } else if (newSession.getStart().equals(oldEvent.getStart().getDateTime())
                    && newSession.getEnd().equals(oldEvent.getEnd().getDateTime())
                    && (Constants.EVENT_SUMMERY_PREFIX + newSession.getCourseName()).equals(oldEvent.getSummary())
                    && newSession.getDescription().equals(oldEvent.getDescription())) {
                // overlap detected, but the new session is exactly the same as the old event,
                // so add the index to the list of indices to be removed later
                newSessionsIndicesToBeRemoved.add(newSessionIndex);
                newSessionIndex++;
                oldEventIndex++;

            } else {
                // overlap detected, add old event to list of overlapping events
                overlappingEvents.add(oldEvent);
                oldEventIndex++;
            }

        }
        // removes all the events that are duplicates from the sessions list
        for (int i = newSessionsIndicesToBeRemoved.size() - 1; i >= 0; i--) {
            sessionsList.remove(newSessionsIndicesToBeRemoved.get(i).intValue());
        }

        return overlappingEvents;
    }

    /**
     * performs a scan on the user events and gather some information.
     * if no full day events found, performs generate PlanIt calendar
     *
     * @param subjectID the user's sub value
     * @param start     the user's preferred start time to generate from (in ISO format)
     * @param end       the user's preferred end time to generate to (in ISO format)
     * @return a {@link DTOscanResponseToController} represents the information that should be returned to the scan controller
     */
    public DTOscanResponseToController scanUserEvents(String subjectID, String start, String end) {

        Instant measureTimeInstant;

        StudyPlan studyPlan = new StudyPlan();
        studyPlan.setStartDateTimeOfPlan(start);
        studyPlan.setEndDateTimeOfPlan(end);
        try {

            // check if user exist in DB
            Optional<User> maybeUser = userRepo.findUserBySubjectID(subjectID);
            if (maybeUser.isEmpty()) {
                return new DTOscanResponseToController(false, Constants.ERROR_USER_NOT_FOUND, HttpStatus.UNAUTHORIZED);
            }

            // get instance of the user
            User user = maybeUser.get();
            // refresh access_token before making api call
            validateAccessTokenExpireTime(user);

            // check if access_token still have scope for Google calendar
            if (!hasAuthorizeScopesStillValid(user.getAuth().getAccessToken())) {
                return new DTOscanResponseToController(false, Constants.ERROR_NO_VALID_ACCESS_TOKEN, HttpStatus.UNAUTHORIZED);
            }

            // 1# get List of user's events
            // perform a scan on the user's Calendar to get all of his events at the time interval
            logger.debug("user " + subjectID + ": before getting calendar information.");
            measureTimeInstant = Instant.now();
            DTOuserCalendarsInformation userCalendarsInformation = getUserCalendarsInformation(user, start, end);
            logger.debug("user " + subjectID + ": after getting calendar information. " + measureTimeInstant.until(Instant.now(), ChronoUnit.MILLIS));

            // fullDayEvents - a list of events that represents the user's full day events
            List<Event> fullDayEvents = userCalendarsInformation.getFullDayEvents();

            // events - a list of events that represents all the user's events
            // planItCalendarOldEvents - a list of PlanIt calendar old events
            List<Event> regularEvents = userCalendarsInformation.getEvents();
            List<Event> planItCalendarOldEvents = userCalendarsInformation.getPlanItCalendarOldEvents();
            List<Exam> examsFound = userCalendarsInformation.getExamsFound();
            studyPlan.convertAndSetScannedExamsAsClientRepresentation(examsFound);
            // checks if no exams are
            if (examsFound.size() == 0) {
                return new DTOscanResponseToController(false, Constants.ERROR_NO_EXAMS_FOUND, HttpStatus.CONFLICT, fullDayEvents);
            }


            if (fullDayEvents.size() != 0) {

                fullDayEvents = HolidaysEngine.handleHolidaysInFullDaysEvents(fullDayEvents, regularEvents
                        , user.getUserPreferences().isStudyOnHolidays(), holidays.getHolidays());

                // after we delete all the event we can. we send the rest of the fullDayEvents we don`t know how to handle.
                if (fullDayEvents.size() != 0) {

                    // return the user with the updated list of fullDayEvents.
                    return new DTOscanResponseToController(false, Constants.UNHANDLED_FULL_DAY_EVENTS, HttpStatus.OK, fullDayEvents, new StudyPlan());
                }

            }

            generatePlanItCalendar(regularEvents,
                    userCalendarsInformation.getExamsFound(),
                    maybeUser.get(),
                    userCalendarsInformation.getCalendarService(),
                    start,
                    planItCalendarOldEvents,
                    studyPlan);

            user.setLatestStudyPlan(studyPlan);
            userRepo.save(user);

        } catch (TokenResponseException e) {
            // e.g. when the refresh token has expired
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST.value() && e.getDetails().getError().equals("invalid_grant")) {
                return new DTOscanResponseToController(false, Constants.ERROR_INVALID_GRANT, HttpStatus.BAD_REQUEST, new ArrayList<>(), new StudyPlan());
            }
        } catch (IOException e) {
            // e.g. when we call Google API with execute() method
            return new DTOscanResponseToController(false, Constants.ERROR_FROM_GOOGLE_API_EXECUTE, HttpStatus.INTERNAL_SERVER_ERROR, new ArrayList<>(), new StudyPlan());
        } catch (GeneralSecurityException e) {
            // e.g. could not create HTTP secure connection
            return new DTOscanResponseToController(false, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, new ArrayList<>(), new StudyPlan());
        }

        return new DTOscanResponseToController(true, Constants.NO_PROBLEM, HttpStatus.CREATED, studyPlan);
    }

    /**
     * performs a scan on the user events and gather some information.
     * then, performs generate PlanIt calendar after handling full days events' user's decisions
     *
     * @param sub           the user's sub value
     * @param start         the user's preferred start time to generate from (in ISO format)
     * @param end           the user's preferred end time to generate to (in ISO format)
     * @param userDecisions an array of boolean that represents the full day events' user's decisions
     * @return a {@link DTOgenerateResponseToController} represents the information that should be returned to the scan controller
     */
    public DTOgenerateResponseToController generateStudyEvents(String sub, String start, String end, Map<Long, Boolean> userDecisions) {

        StudyPlan studyPlan = new StudyPlan();
        studyPlan.setStartDateTimeOfPlan(start);
        studyPlan.setEndDateTimeOfPlan(end);
        try {
            // check if user exist in DB
            Optional<User> maybeUser = userRepo.findUserBySubjectID(sub);
            if (maybeUser.isEmpty()) {
                return new DTOgenerateResponseToController(false, ERROR_USER_NOT_FOUND, HttpStatus.UNAUTHORIZED);
            }

            // get instance of the user
            User user = maybeUser.get();

            // 1# get List of user's events
            // perform a scan on the user's Calendar to get all of his events at the time interval
            DTOuserCalendarsInformation userCalendarsInformation = getUserCalendarsInformation(user, start, end);
            // fullDayEvents - a list of events that represents the user's full day events
            List<Event> fullDayEvents = userCalendarsInformation.getFullDayEvents();
            // planItCalendarOldEvents - a list of PlanIt calendar old events
            List<Event> planItCalendarOldEvents = userCalendarsInformation.getPlanItCalendarOldEvents();
            // events - a list of events that represents all the user's events
            List<Event> regularEvents = userCalendarsInformation.getEvents();
            List<Exam> examsFound = userCalendarsInformation.getExamsFound();
            studyPlan.convertAndSetScannedExamsAsClientRepresentation(examsFound);

            // check if fullDayEvents List is empty (which doesn't suppose to be)
            if (fullDayEvents.size() != 0) {

                fullDayEvents = HolidaysEngine.handleHolidaysInFullDaysEvents(fullDayEvents, regularEvents
                        , user.getUserPreferences().isStudyOnHolidays(), holidays.getHolidays());

                // go through the list of full day events
                for (Event fullDayEvent : fullDayEvents) {

                    boolean userWantToStudyAtCurrentFullDayEvent = userDecisions.get(fullDayEvent.getStart().getDate().getValue());

                    // check if user want to study at the current fullDayEvent
                    if (!userWantToStudyAtCurrentFullDayEvent) {
                        // change the full day event to regular event and add it to the regularEvents
                        Event convertedFullDayEvent = Utility.convertFullDayEventToRegularEvent(fullDayEvent);
                        regularEvents.add(convertedFullDayEvent);
                    }
                }

                regularEvents.sort(new EventComparator());
            }

            // 2# 3# 4# 5#
            generatePlanItCalendar(regularEvents, userCalendarsInformation.getExamsFound(), maybeUser.get(), userCalendarsInformation.getCalendarService(), start, planItCalendarOldEvents, studyPlan);

            user.setLatestStudyPlan(studyPlan);
            userRepo.save(user);

        } catch (TokenResponseException e) {
            // e.g. when the refresh token has expired
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST.value() && e.getDetails().getError().equals("invalid_grant")) {
                return new DTOgenerateResponseToController(false, Constants.ERROR_INVALID_GRANT, HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            // e.g. when we call Google API with execute() method
            return new DTOgenerateResponseToController(false, Constants.ERROR_FROM_GOOGLE_API_EXECUTE, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (GeneralSecurityException e) {
            // e.g. could not create HTTP secure connection
            return new DTOgenerateResponseToController(false, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new DTOgenerateResponseToController(true, Constants.NO_PROBLEM, HttpStatus.CREATED, studyPlan);
    }

    public static boolean hasAuthorizeScopesStillValid(String access_token) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + access_token)
                .build();
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        JSONObject jsonObject = new JSONObject(responseBody);

        // if response is >= 200 && < 300
        if (response.isSuccessful()) {

            // continue to check for required scope
            String scopes = (String) jsonObject.get("scope");
            String[] scopesArray = scopes.split(" ");

            for (String scope : scopesArray) {
                if (scope.equals("https://www.googleapis.com/auth/calendar")) {
                    return true; // found scope
                }
            }
        }
        return false; // scope was not found or response wasn't successful
    }

    public DTOstudyPlanResponseToController getUserLatestStudyPlan(String sub) {
        Optional<User> maybeUser = userRepo.findUserBySubjectID(sub);

        if (maybeUser.isEmpty()) {
            return new DTOstudyPlanResponseToController(false, ERROR_UNAUTHORIZED_USER, HttpStatus.UNAUTHORIZED);
        }
        return new DTOstudyPlanResponseToController(true, NO_PROBLEM, HttpStatus.OK, maybeUser.get().getLatestStudyPlan());
    }

    private void setHolidaysFromCalendar(Calendar calendarService, DateTime start, DateTime end) {
        Events events;

        if (holidays != null) {
            return;
        }
        try {
            events = calendarService.events().list(Constants.CALENDAR_HOLIDAYS_ID_IN_GOOGLE)
                    .setTimeMin(start)
                    .setOrderBy("startTime")
                    .setTimeMax(end)
                    .setSingleEvents(true)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Event event : events.getItems()) {

            holidays.getHolidays().add(event.getStart().getDate().toStringRfc3339());
        }
    }

}

/*

<string, int>
<name, show>


Priority Queue:    [           B   C           ]

name      show     priority
C          5       First


[A][A][A][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]testA[C][B][B][B]testB[C][C][C][C]testC
               {A,B,C}
                1 2 3



[ ][ ][ ][ ][ ]testA[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]testB

after embed the exam's course names we know, for each course:
 numOfSessions
 numOfSubjects

 case1: numOfSessions > numOfSubjects
 - more than one subjects for each session.

 case2: numOfSubjects < numOfSessions
 - each subject for more than one session.

 case3: numOfSubjects = numOfSessions
 - each subject gets one session





 CCCBBAAAAAAA testA - testB CCCCC testC
    {A,B,C}               {B,C}          {C}

<<String, int>,       int>
<<name,   difficult>, show>
<-----------------------------------------------
A   B   C
3   3   5                Coman              OS &LINUX
[A][A][A]testA[C][B][B][B]testB[C][C][C][C]testC

A   B   C
3   4   5
[A][A][A]testA[B/C][B][B][B]testB[C][C][C][C]testC ?

A   B   C
4   2   6
[A][A][A]testA[C][C][B][B]testB[C][C][C][C]testC

A   B   C
4   2   5
[A][A][A]testA[ ][C][B][B]testB[C][C][C][C]testC ?

A   B   C
4   5   3
[A][A][A]testA[B][B][B][B]testB[ ][C][C][C]testC
* */
package com.example.planit.engine;

import com.example.planit.model.exam.Exam;
import com.example.planit.model.mongo.course.Course;
import com.example.planit.model.mongo.course.CoursesRepository;
import com.example.planit.model.mongo.user.User;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.model.studysession.StudySession;
import com.example.planit.model.timeslot.TimeSlot;
import com.example.planit.utill.Constants;
import com.example.planit.utill.EventComparator;
import com.example.planit.utill.Utility;
import com.example.planit.utill.dto.DTOfreetime;
import com.example.planit.utill.dto.DTOstartAndEndOfInterval;
import com.example.planit.utill.dto.DTOuserCalendarsInformation;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.example.planit.utill.Constants.*;
import static com.example.planit.utill.Utility.roundInstantMinutesTime;

public class CalendarEngine {

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

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
     */
    private static String createPlanItCalendar(Calendar calendarService, User user, UserRepository userRepo) {

        String planItCalendarID;

        String planItCalendarIdFromDB = user.getPlanItCalendarID();

        // checks if the calendar already exists in DB
        try {
            if (planItCalendarIdFromDB != null && calendarService.calendars().get(planItCalendarIdFromDB).execute() != null) {
                return planItCalendarIdFromDB;
            }
        } catch (IOException ignored) {
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
            createdCalendar = calendarService.calendars().insert(calendar).execute();
        } catch (IOException e) {
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
            if (startOfSession.toEpochMilli() < timeSlot.getEnd().getValue()) {
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
    private static Map<Exam, Integer> distributeNumberOfSessionsToCourses(Map<Exam, Double> exam2Proportions, int numOfSessions) {
        Map<Exam, Integer> exams2numberOfSessions = new HashMap<>();

        for (Map.Entry<Exam, Double> examProportionMapEntry : exam2Proportions.entrySet()) {
            Exam exam = examProportionMapEntry.getKey();
            Double proportion = examProportionMapEntry.getValue();

            // gets the number of sessions for the current exam
            Integer numberOfSessionForCurrentExam = (int) Math.round(numOfSessions * proportion);

            // adds the number of sessions to the map
            exams2numberOfSessions.put(exam, numberOfSessionForCurrentExam);
        }

        return exams2numberOfSessions;

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
        //Collections.nCopies(exams.size(), new ArrayList<>())

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

            double subjectsPerSession = (double) subjects.length / (double) numberOfSessionsForSubjects;
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
    private static void updatePlanItCalendar(List<StudySession> sessionsList, Calendar service, String planItCalendarID, List<Event> planItCalendarOldEvents) {

        List<Event> overlapsOldEvents = getOverlapOldEventsPlanItCalendar(sessionsList, planItCalendarOldEvents);

        for (Event eventToBeDeleted : overlapsOldEvents) {
            try {
                // removes overlap events in the PlanIt calendar
                service.events().delete(planItCalendarID, eventToBeDeleted.getId()).execute();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        // adds updated events to the PlanIt calendar
        // goes through the sessions and adds to the PlanIt calendar
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
                service.events().insert(planItCalendarID, event).execute();

            } catch (GoogleJsonResponseException e) {
                System.out.println(e.getDetails());
                System.out.println(e.getStatusCode());
                System.out.println(e.getMessage());
                throw new RuntimeException();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * finds all the overlapping events from the old PlanIt calendar (from previous generating processes).
     * the new generated sessions are compared to the old generated events.
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
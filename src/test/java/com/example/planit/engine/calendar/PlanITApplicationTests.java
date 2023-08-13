package com.example.planit.engine.calendar;

import com.example.planit.engine.CalendarEngine;
import com.example.planit.utill.Constants;
import com.example.planit.utill.dto.DTOscanResponseToController;
import com.google.api.services.calendar.model.Event;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PlanITApplicationTests {
    @Autowired
    private CalendarEngine calendarEngine;

    private final String subjectIDForTestInput = "112510677559500692451";

    private final Map<Long, Boolean> decisions = new HashMap<>();

    @Test
    void noExams() {
        String expectedOutput = Constants.ERROR_NO_EXAMS_FOUND;
        String actualOutput = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
                "2022-12-31T22:00:00.000Z",
                "2023-01-01T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void OneExam() {
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
                "2023-01-04T22:00:00.000Z",
                "2023-01-15T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void moreThenOneExam() {
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
                "2023-01-15T22:00:00.000Z",
                "2023-01-31T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void unrecognizedCourseName() {
        String expectedOutput = Constants.ERROR_NO_EXAMS_FOUND;
        String actualOutput = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
                "2023-02-01T22:00:00.000Z",
                "2023-02-15T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void courseNameInHebrew() {
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
                "2023-02-16T22:00:00.000Z",
                "2023-02-27T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void courseNameInEnglish() {
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
                "2023-02-26T22:00:00.000Z",
                "2023-03-05T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void oneExamWithFullDayEvents() {
        String expectedOutput = Constants.UNHANDLED_FULL_DAY_EVENTS;
        String actualOutput = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
                "2023-03-16T22:00:00.000Z",
                "2023-03-29T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void threeExamsWithTwoWeeks() {
        String expectedOutput = Constants.UNHANDLED_FULL_DAY_EVENTS;
        String actualOutput = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
                "2023-05-01T22:00:00.000Z",
                "2023-05-15T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void twoExamsWithFourWeeks() {
        String expectedOutput = Constants.UNHANDLED_FULL_DAY_EVENTS;
        String actualOutput = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
                "2023-05-16T22:00:00.000Z",
                "2023-06-15T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void userNotFound() {
        String expectedOutput = Constants.ERROR_USER_NOT_FOUND;
        String actualOutput = calendarEngine.generateNewStudyPlan(
                "1",
                "2022-12-31T22:00:00.000Z",
                "2023-01-31T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void holidaysFoundInCalendar() {
        String expectedOutput = Constants.UNHANDLED_FULL_DAY_EVENTS;
        DTOscanResponseToController scanResponse = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
                "2023-05-07T22:00:00.000Z",
                "2023-05-28T21:59:59.000Z",
                decisions);
        String actualOutput = scanResponse.getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void scanWithDecisions() {
        decisions.put(1686009600000L, true);
        String expectedOutput = Constants.NO_PROBLEM;
        DTOscanResponseToController scanResponse = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
                "2023-06-01T22:00:00.000Z",
                "2023-06-09T21:59:59.000Z",
                decisions);
        String actualOutput = scanResponse.getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void RegeneratePlanWithFullDaysEvent() {
        String expectedOutput = Constants.NO_PROBLEM;
        decisions.put(1690329600000L, true);
        decisions.put(1690416000000L, true);
        decisions.put(1690329600000L, true);
        DTOscanResponseToController scanResponse = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
//                "2023-08-30T22:00:00.000Z",
//                "2023-09-09T21:59:59.000Z",
                "2023-07-23T10:00:00.000Z",
                "2023-08-04T21:59:59.000Z",
                decisions);

        if (scanResponse.isSucceed()) {
            DTOscanResponseToController regeneratePlanResponse = calendarEngine.regenerateStudyPlan(
                    subjectIDForTestInput,
                    decisions,
                    Instant.parse("2023-07-31T22:00:00.000Z"));

            String actualOutput = regeneratePlanResponse.getDetails();
            Assertions.assertEquals(expectedOutput, actualOutput);
        }
    }

    @Test
    void RegeneratePlanWithoutFullDaysEvent() {
        String expectedOutput = Constants.NO_PROBLEM;
        DTOscanResponseToController scanResponse = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
                "2023-08-06T00:00:00.000Z",
                "2023-08-12T21:59:59.000Z",
                decisions);

        if (scanResponse.isSucceed()) {
            DTOscanResponseToController regeneratePlanResponse = calendarEngine.regenerateStudyPlan(
                    subjectIDForTestInput,
                    decisions,
                    Instant.parse("2023-08-07T22:00:00.000Z"));

            String actualOutput = regeneratePlanResponse.getDetails();
            Assertions.assertEquals(expectedOutput, actualOutput);
        }
    }

    @Test
    void duplicateFullDayEventsDetection() {
        // Generate study plan and get full-day events
        DTOscanResponseToController scanResponse = calendarEngine.generateNewStudyPlan(
                subjectIDForTestInput,
                "2023-05-06T00:00:00.000Z",
                "2023-08-12T21:59:59.000Z",
                decisions);

        List<Event> fullDayEvents = scanResponse.getFullDayEvents();

        // Use Java streams to find duplicate events
        boolean hasDuplicates = fullDayEvents.stream()
                .anyMatch(event -> fullDayEvents.stream()
                        .anyMatch(otherEvent ->
                                event != otherEvent &&
                                        event.getStart().getDate().getValue() == otherEvent.getStart().getDate().getValue() &&
                                        event.getSummary().equals(otherEvent.getSummary())
                        ));

        // Assert that there are no duplicate events
        assertFalse(hasDuplicates, "Duplicate full-day events found.");
    }
}

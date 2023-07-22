package com.example.planit.engine.calendar;

import com.example.planit.engine.CalendarEngine;
import com.example.planit.utill.Constants;
import com.example.planit.utill.dto.DTOscanResponseToController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

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
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2022-12-31T22:00:00.000Z",
                "2023-01-01T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void OneExam() {
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-01-04T22:00:00.000Z",
                "2023-01-15T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void moreThenOneExam() {
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-01-15T22:00:00.000Z",
                "2023-01-31T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void unrecognizedCourseName() {
        String expectedOutput = Constants.ERROR_NO_EXAMS_FOUND;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-02-01T22:00:00.000Z",
                "2023-02-15T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void courseNameInHebrew() {
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-02-16T22:00:00.000Z",
                "2023-02-27T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void courseNameInEnglish() {
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-02-26T22:00:00.000Z",
                "2023-03-05T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void oneExamWithFullDayEvents() {
        String expectedOutput = Constants.UNHANDLED_FULL_DAY_EVENTS;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-03-16T22:00:00.000Z",
                "2023-03-29T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void threeExamsWithTwoWeeks() {
        String expectedOutput = Constants.UNHANDLED_FULL_DAY_EVENTS;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-05-01T22:00:00.000Z",
                "2023-05-15T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void twoExamsWithFourWeeks() {
        String expectedOutput = Constants.UNHANDLED_FULL_DAY_EVENTS;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-05-16T22:00:00.000Z",
                "2023-06-15T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void userNotFound() {
        String expectedOutput = Constants.ERROR_USER_NOT_FOUND;
        String actualOutput = calendarEngine.scanUserEvents(
                "1",
                "2022-12-31T22:00:00.000Z",
                "2023-01-31T21:59:59.000Z",
                decisions).getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void holidaysFoundInCalendar() {
        String expectedOutput = Constants.UNHANDLED_FULL_DAY_EVENTS;
        DTOscanResponseToController scanResponse = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-05-07T22:00:00.000Z",
                "2023-05-28T21:59:59.000Z",
                decisions);
        String actualOutput = scanResponse.getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
        Assertions.assertEquals(actualOutput, expectedOutput);
    }

    @Test
    void scanWithDecisions() {
        decisions.put(1686009600000L, true);
        String expectedOutput = Constants.NO_PROBLEM;
        DTOscanResponseToController scanResponse = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-06-01T22:00:00.000Z",
                "2023-06-09T21:59:59.000Z",
                decisions);
        String actualOutput = scanResponse.getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
        Assertions.assertEquals(actualOutput, expectedOutput);
    }
}

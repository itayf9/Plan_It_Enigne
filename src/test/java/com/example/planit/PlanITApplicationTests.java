package com.example.planit;

import com.example.planit.engine.CalendarEngine;
import com.example.planit.holidays.PlanITHolidays;
import com.example.planit.model.mongo.course.CoursesRepository;
import com.example.planit.model.mongo.holiday.Holiday;
import com.example.planit.model.mongo.holiday.HolidayRepository;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.Constants;
import com.example.planit.utill.dto.DTOscanResponseToController;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.util.List;

@SpringBootTest
class PlanITApplicationTests {

    @Autowired
    private Environment env;

    @Autowired
    private CoursesRepository courseRepo;

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private HolidayRepository holidayRepo;

    private CalendarEngine calendarEngine;
    @Autowired
    private PlanITHolidays holidays;
    private final String subjectIDForTestInput = "112510677559500692451";

    @BeforeEach
    public void init() {
        String clientId = env.getProperty("spring.security.oauth2.client.registration.google.client-id");
        String clientSecret = env.getProperty("spring.security.oauth2.client.registration.google.client-secret");
        holidays.setHolidays(holidayRepo.findAll());

        // initialize CalendarEngine
        this.calendarEngine = new CalendarEngine(clientId, clientSecret, userRepo, courseRepo, holidays);
    }

    @Test
    void noExams() {
        String expectedOutput = Constants.ERROR_NO_EXAMS_FOUND;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2022-12-31T22:00:00.000Z",
                "2023-01-01T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void OneExam() {
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-01-04T22:00:00.000Z",
                "2023-01-15T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void moreThenOneExam() {
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-01-15T22:00:00.000Z",
                "2023-01-31T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void unrecognizedCourseName() {
        String expectedOutput = Constants.ERROR_NO_EXAMS_FOUND;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-02-01T22:00:00.000Z",
                "2023-02-15T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void courseNameInHebrew() {
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-02-16T22:00:00.000Z",
                "2023-02-27T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void courseNameInEnglish() {
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-02-26T22:00:00.000Z",
                "2023-03-05T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void oneExamWithFullDayEvents() {
        String expectedOutput = Constants.UNHANDLED_FULL_DAY_EVENTS;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-03-16T22:00:00.000Z",
                "2023-03-29T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void threeExamsWithTwoWeeks() {
        String expectedOutput = Constants.UNHANDLED_FULL_DAY_EVENTS;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-05-01T22:00:00.000Z",
                "2023-05-15T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void twoExamsWithFourWeeks() {
        String expectedOutput = Constants.UNHANDLED_FULL_DAY_EVENTS;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-05-16T22:00:00.000Z",
                "2023-06-15T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void userNotFound() {
        String expectedOutput = Constants.ERROR_USER_NOT_FOUND;
        String actualOutput = calendarEngine.scanUserEvents(
                "1",
                "2022-12-31T22:00:00.000Z",
                "2023-01-31T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void holidaysFoundInCalendar() {
        String expectedOutput = Constants.UNHANDLED_FULL_DAY_EVENTS;
        DTOscanResponseToController scanResponse = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-05-07T22:00:00.000Z",
                "2023-05-28T21:59:59.000Z");
        String actualOutput = scanResponse.getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
        Assertions.assertEquals(actualOutput, expectedOutput);
    }
}

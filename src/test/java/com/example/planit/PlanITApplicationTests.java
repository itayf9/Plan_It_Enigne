package com.example.planit;

import com.example.planit.engine.CalendarEngine;
import com.example.planit.engine.HolidaysEngine;
import com.example.planit.model.mongo.course.CoursesRepository;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.Constants;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.time.ZonedDateTime;
import java.util.Set;

import static com.example.planit.utill.Constants.ISRAEL_HOLIDAYS_CODE;

@SpringBootTest
class PlanITApplicationTests {

    @Autowired
    private Environment env;

    @Autowired
    private CoursesRepository courseRepo;

    @Autowired
    private UserRepository userRepo;

    private CalendarEngine calendarEngine;

    private final String subjectIDForTestInput = "112510677559500692451";

    @Before
    public void init() {
        String clientId = env.getProperty("spring.security.oauth2.client.registration.google.client-id");
        String clientSecret = env.getProperty("spring.security.oauth2.client.registration.google.client-secret");
        String holidaysApiKey = env.getProperty("holidays_api_key");
        Set<String> holidaysDatesCurrentYear = HolidaysEngine.getDatesOfHolidays(holidaysApiKey, ISRAEL_HOLIDAYS_CODE, ZonedDateTime.now().getYear());
        Set<String> holidaysDatesNextYear = HolidaysEngine.getDatesOfHolidays(holidaysApiKey, ISRAEL_HOLIDAYS_CODE, ZonedDateTime.now().getYear() + 1);
        calendarEngine = new CalendarEngine(clientId, clientSecret, userRepo, courseRepo, holidaysDatesCurrentYear, holidaysDatesNextYear);
    }

    @Test
    void noExams() {
        init();
        String expectedOutput = Constants.ERROR_NO_EXAMS_FOUND;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2022-12-31T22:00:00.000Z",
                "2023-01-01T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void OneExam() {
        init();
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-01-01T22:00:00.000Z",
                "2023-01-15T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void moreThenOneExam() {
        init();
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-01-15T22:00:00.000Z",
                "2023-01-31T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void unrecognizedCourseName() {
        init();
        String expectedOutput = Constants.ERROR_NO_EXAMS_FOUND;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-02-01T22:00:00.000Z",
                "2023-02-15T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void courseNameInHebrew() {
        init();
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-02-16T22:00:00.000Z",
                "2023-02-27T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void courseNameInEnglish() {
        init();
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-03-01T22:00:00.000Z",
                "2023-03-15T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void oneExamWithFullDayEvents() {
        init();
        String expectedOutput = Constants.UNHANDLED_FULL_DAY_EVENTS;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-03-16T22:00:00.000Z",
                "2023-03-29T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void threeExamsWithTwoWeeks() {
        init();
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-05-01T22:00:00.000Z",
                "2023-05-15T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void twoExamsWithFourWeeks() {
        init();
        String expectedOutput = Constants.NO_PROBLEM;
        String actualOutput = calendarEngine.scanUserEvents(
                subjectIDForTestInput,
                "2023-05-16T22:00:00.000Z",
                "2023-06-15T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void userNotFound() {
        init();
        String expectedOutput = Constants.ERROR_USER_NOT_FOUND;
        String actualOutput = calendarEngine.scanUserEvents(
                "1",
                "2022-12-31T22:00:00.000Z",
                "2023-01-31T21:59:59.000Z").getDetails();
        Assertions.assertEquals(expectedOutput, actualOutput);
    }
}

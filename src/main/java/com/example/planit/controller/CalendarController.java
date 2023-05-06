package com.example.planit.controller;

import com.example.planit.engine.CalendarEngine;
import com.example.planit.model.mongo.course.CoursesRepository;
import com.example.planit.model.mongo.holiday.Holiday;
import com.example.planit.model.mongo.holiday.HolidayRepository;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.dto.*;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class CalendarController {

    @Autowired
    private Environment env;

    @Autowired
    private CoursesRepository courseRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private HolidayRepository holidayRepo;

    public static Logger logger = LogManager.getLogger(CalendarController.class);

    private CalendarEngine calendarEngine;

    @PostConstruct
    private void init() {

        // get CLIENT_ID & CLIENT_SECRET values from environment
        String CLIENT_ID = env.getProperty("spring.security.oauth2.client.registration.google.client-id");
        String CLIENT_SECRET = env.getProperty("spring.security.oauth2.client.registration.google.client-secret");

        // get Holidays from db and create set contains iso string of all holidays
        List<Holiday> holidays = holidayRepo.findAll();
        Set<String> holidaysDates = new HashSet<>();

        holidays.forEach(holiday -> holidaysDates.add(holiday.getHolidayStartDate()));

        // initialize CalendarEngine
        this.calendarEngine = new CalendarEngine(CLIENT_ID, CLIENT_SECRET, userRepo, courseRepo, holidaysDates);

    }

    /**
     * Scan the user's Calendar to get list of events and check to see if user has fullDayEvents existed.
     *
     * @param sub user's sub value to search the User on DB & get preferences.
     * @return ResponseEntity<List < Event>> we return list of events in a case of full day events found, otherwise we generate the calendar.
     */
    @PostMapping(value = "/scan")
    public ResponseEntity<DTOscanResponseToClient> scanUserEvents(@RequestParam String sub, @RequestParam String start, @RequestParam String end) {

        long s = System.currentTimeMillis();
        logger.info(MessageFormat.format("User {0}: has requested POST /scan with params: sub={0}, start={1}, end={2}", sub, start, end));

        DTOscanResponseToController scanResponseToController = calendarEngine.scanUserEvents(sub, start, end);
        long t = System.currentTimeMillis();
        long res = t - s;
        logger.info(MessageFormat.format("User {0}: scan time is {1} ms", sub, res));

        return ResponseEntity.status(scanResponseToController.getHttpStatus())
                .body(new DTOscanResponseToClient(scanResponseToController.isSucceed(),
                        scanResponseToController.getDetails(),
                        scanResponseToController.getFullDayEvents(), scanResponseToController.getStudyPlan()));


    }

    /**
     * this endpoint re-scan the user Calendar events, deals with the full days events that has been found and generates the plan it calendar.
     *
     * @param sub       user's sub value (as string), to search the User on DB & get preferences
     * @param decisions array of boolean values representing the user's decisions for each of the full day events that were found
     * @return ResponseEntity<String> this method not suppose to fail unless it's been called externally
     */
    @PostMapping(value = "/generate", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<DTOstatus> generateStudyEvents(@RequestParam String sub, @RequestParam String start, @RequestParam String end, @RequestBody Map<Long, Boolean> decisions) {

        long s = System.currentTimeMillis();
        logger.info(MessageFormat.format("User {0}: has requested POST /generate with params: sub={0}, start={1}, end={2}, decisions={3}", sub, start, end, decisions.toString()));

        DTOgenerateResponseToController generateResponseToController = calendarEngine.generateStudyEvents(sub, start, end, decisions);
        long t = System.currentTimeMillis();
        long res = t - s;
        logger.info(MessageFormat.format("User {0}: generate time is {1} ms", sub, res));

        return ResponseEntity.status(generateResponseToController.getHttpStatus())
                .body(new DTOgenerateResponseToClient(generateResponseToController.isSucceed(),
                        generateResponseToController.getDetails(), generateResponseToController.getStudyPlan()));


    }

    @GetMapping(value = "/study-plan")
    public ResponseEntity<DTOstudyPlanResponseToClient> getLatestStudyPlan(@RequestParam String sub) {

        long s = System.currentTimeMillis();
        logger.info(MessageFormat.format("User {0}: has requested POST /study-plan with params: sub={0}", sub));

        DTOstudyPlanResponseToController dtOstudyPlanResponseToController = calendarEngine.getUserLatestStudyPlan(sub);

        long t = System.currentTimeMillis();
        long res = t - s;
        logger.info(MessageFormat.format("User {0}: study-plan time is {1} ms", sub, res));

        return ResponseEntity.status(dtOstudyPlanResponseToController.getHttpStatus())
                .body(new DTOstudyPlanResponseToClient(dtOstudyPlanResponseToController.isSucceed(),
                        dtOstudyPlanResponseToController.getDetails(), dtOstudyPlanResponseToController.getStudyPlan()));
    }
}

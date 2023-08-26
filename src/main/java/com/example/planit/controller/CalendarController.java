package com.example.planit.controller;

import com.example.planit.engine.CalendarEngine;
import com.example.planit.holidays.PlanITHolidaysWrapper;
import com.example.planit.model.mongo.holiday.HolidayRepository;
import com.example.planit.utill.dto.*;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;
import java.util.Map;

@CrossOrigin
@RestController
public class CalendarController {

    @Autowired
    private HolidayRepository holidayRepo;

    public static Logger logger = LogManager.getLogger(CalendarController.class);

    @Autowired
    private PlanITHolidaysWrapper holidaysWrapper;

    @Autowired
    private CalendarEngine calendarEngine;

    @PostConstruct
    private void init() {
        holidaysWrapper.setHolidays(holidayRepo.findAll());
        logger.info("System is up successfully");
    }

    /**
     * Scan the user's Calendar to get list of events and check to see if user has fullDayEvents existed.
     *
     * @param sub user's sub value to search the User on DB & get preferences.
     * @return ResponseEntity<List < Event>> we return list of events in a case of full day events found, otherwise we generate the calendar.
     */
    @PostMapping(value = "/scan", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<DTOscanResponseToClient> scanUserEvents(@RequestParam String sub, @RequestParam String start,
                                                                  @RequestParam String end, @RequestBody(required = false) Map<Long, Boolean> decisions) {

        long s = System.currentTimeMillis();
        logger.info(MessageFormat.format("User {0}: has requested POST /scan with params: sub={0}, start={1}, end={2}", sub, start, end));

        DTOscanResponseToController scanResponseToController = calendarEngine.generateNewStudyPlan(sub, start, end, decisions);

        logger.info(MessageFormat.format("User {0}: scan time is {1} ms", sub, (System.currentTimeMillis() - s)));

        return ResponseEntity.status(scanResponseToController.getHttpStatus())
                .body(new DTOscanResponseToClient(
                        scanResponseToController.isSucceed(),
                        scanResponseToController.getDetails(),
                        scanResponseToController.getFullDayEvents(),
                        scanResponseToController.getStudyPlan(),
                        scanResponseToController.getUpComingSession()));
    }

    @PostMapping(value = "/re-generate", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<DTOscanResponseToClient> regenerateStudyPlan(@RequestParam String sub, @RequestBody(required = false) Map<Long, Boolean> decisions) {

        long s = System.currentTimeMillis();
        logger.info(MessageFormat.format("User {0}: has requested POST /re-generate with params: sub={0}", sub));

        DTOscanResponseToController scanResponseToController = calendarEngine.regenerateStudyPlan(sub, decisions);

        logger.info(MessageFormat.format("User {0}: scan time is {1} ms", sub, System.currentTimeMillis() - s));

        return ResponseEntity.status(scanResponseToController.getHttpStatus())
                .body(new DTOscanResponseToClient(
                        scanResponseToController.isSucceed(),
                        scanResponseToController.getDetails(),
                        scanResponseToController.getFullDayEvents(),
                        scanResponseToController.getStudyPlan(),
                        scanResponseToController.getUpComingSession()));
    }

    @GetMapping(value = "/study-plan")
    public ResponseEntity<DTOstudyPlanAndSessionResponseToClient> getLatestStudyPlan(@RequestParam String sub) {

        long s = System.currentTimeMillis();
        logger.info(MessageFormat.format("User {0}: has requested POST /study-plan with params: sub={0}", sub));

        DTOstudyPlanAndSessionResponseToController studyPlanAndSessionResponseToController = calendarEngine.getUserLatestStudyPlanAndUpComingSession(sub);

        logger.info(MessageFormat.format("User {0}: study-plan time is {1} ms", sub, System.currentTimeMillis() - s));


        return ResponseEntity.status(studyPlanAndSessionResponseToController.getHttpStatus())
                .body(new DTOstudyPlanAndSessionResponseToClient(
                        studyPlanAndSessionResponseToController.isSucceed(),
                        studyPlanAndSessionResponseToController.getDetails(),
                        studyPlanAndSessionResponseToController.getStudyPlan(),
                        studyPlanAndSessionResponseToController.getUpComingSession()));
    }

    @DeleteMapping(value = "/study-plan")
    public ResponseEntity<DTOstatus> removeLatestStudyPlan(@RequestParam String sub) {

        long s = System.currentTimeMillis();
        logger.info(MessageFormat.format("User {0}: has requested DELETE /study-plan with params: sub={0}", sub));

        DTOresponseToController responseToController = calendarEngine.removeUserLatestStudyPlanAndUpComingSession(sub);

        logger.info(MessageFormat.format("User {0}: remove study-plan time is {1} ms", sub, System.currentTimeMillis() - s));

        return ResponseEntity.status(responseToController.getHttpStatus())
                .body(new DTOstatus(
                        responseToController.isSucceed(),
                        responseToController.getDetails()
                ));
    }
}

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

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class CalendarController {

    @Autowired
    private HolidayRepository holidayRepo;

    public static Logger logger = LogManager.getLogger(CalendarController.class);

    @Autowired
    PlanITHolidaysWrapper holidays;

    @Autowired
    private CalendarEngine calendarEngine;

    @PostConstruct
    private void init() {
        holidays.setHolidays(holidayRepo.findAll());
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

        DTOscanResponseToController scanResponseToController = calendarEngine.scanUserEvents(sub, start, end, decisions);
        long t = System.currentTimeMillis();
        long res = t - s;
        logger.info(MessageFormat.format("User {0}: scan time is {1} ms", sub, res));

        return ResponseEntity.status(scanResponseToController.getHttpStatus())
                .body(new DTOscanResponseToClient(scanResponseToController.isSucceed(),
                        scanResponseToController.getDetails(),
                        scanResponseToController.getFullDayEvents(), scanResponseToController.getStudyPlan()));
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

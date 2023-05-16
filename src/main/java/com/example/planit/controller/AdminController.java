package com.example.planit.controller;

import com.example.planit.engine.AdminEngine;
import com.example.planit.model.mongo.course.Course;
import com.example.planit.model.mongo.course.CoursesRepository;
import com.example.planit.model.mongo.holiday.HolidayRepository;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.dto.*;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class AdminController {
    @Autowired
    CoursesRepository courseRepo;
    @Autowired
    UserRepository userRepo;
    @Autowired
    private HolidayRepository holidayRepo;
    @Autowired
    private Environment env;

    public static Logger logger = LogManager.getLogger(AdminController.class);

    private AdminEngine adminEngine;

    @PostConstruct
    private void init() {
        this.adminEngine = new AdminEngine(courseRepo, userRepo, holidayRepo, env);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value = "/admin/courses")
    public ResponseEntity<DTOcoursesResponseToClient> getAllCourses(@RequestParam String sub) {
        DTOcoursesResponseToController dtOcoursesResponseToController = adminEngine.getAllCoursesFromDB(sub);

        return ResponseEntity.status(dtOcoursesResponseToController.getHttpStatus())
                .body(new DTOcoursesResponseToClient(dtOcoursesResponseToController.isSucceed(),
                        dtOcoursesResponseToController.getDetails(), dtOcoursesResponseToController.getCourses()));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value = "/admin/courses/{courseId}")
    public ResponseEntity<DTOcoursesResponseToClient> getCourse(@PathVariable String courseId, @RequestParam String sub) {
        DTOcoursesResponseToController dtOcoursesResponseToController = adminEngine.getCourseFromDB(courseId, sub);

        return ResponseEntity.status(dtOcoursesResponseToController.getHttpStatus())
                .body(new DTOcoursesResponseToClient(dtOcoursesResponseToController.isSucceed(),
                        dtOcoursesResponseToController.getDetails(), dtOcoursesResponseToController.getCourses()));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/admin/courses")
    public ResponseEntity<DTOstatus> addCourse(@RequestBody Course course, @RequestParam String sub) {
        DTOcoursesResponseToController dtOcoursesResponseToController = adminEngine.addCourseToDB(course, sub);

        return ResponseEntity.status(dtOcoursesResponseToController.getHttpStatus())
                .body(new DTOstatus(dtOcoursesResponseToController.isSucceed(),
                        dtOcoursesResponseToController.getDetails()));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping(value = "/admin/courses")
    public ResponseEntity<DTOstatus> updateCourse(@RequestBody Course course, @RequestParam String sub) {
        DTOcoursesResponseToController dtOcoursesResponseToController = adminEngine.updateCourseInDB(course, sub);

        return ResponseEntity.status(dtOcoursesResponseToController.getHttpStatus())
                .body(new DTOstatus(dtOcoursesResponseToController.isSucceed(),
                        dtOcoursesResponseToController.getDetails()));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value = "/admin/users")
    public ResponseEntity<DTOstatus> getAllUsers(@RequestParam String sub) {
        DTOusersResponseToController dtOusersResponseToController = adminEngine.getAllUsersFromDB(sub);

        return ResponseEntity.status(dtOusersResponseToController.getHttpStatus())
                .body(new DTOusersResponseToClient(dtOusersResponseToController.isSucceed(),
                        dtOusersResponseToController.getDetails(), dtOusersResponseToController.getUsers()));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping(value = "/admin/make-user-admin")
    public ResponseEntity<DTOstatus> makeUserAdmin(@RequestParam String sub, @RequestParam String userSubId) {
        DTOusersResponseToController dtOusersResponseToController = adminEngine.makeUserAdminInDB(sub, userSubId);

        return ResponseEntity.status(dtOusersResponseToController.getHttpStatus())
                .body(new DTOstatus(dtOusersResponseToController.isSucceed(),
                        dtOusersResponseToController.getDetails()));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping(value = "/admin/update-holidays")
    public ResponseEntity<DTOstatus> updateHolidays(@RequestParam String sub) {

        DTOholidaysResponseToController dtOholidaysResponseToController = adminEngine.updateHolidays(sub);

        return ResponseEntity.status(dtOholidaysResponseToController.getHttpStatus())
                .body(new DTOstatus(dtOholidaysResponseToController.isSucceed(), dtOholidaysResponseToController.getDetails()));
    }

}


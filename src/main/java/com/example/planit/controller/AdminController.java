package com.example.planit.controller;

import com.example.planit.engine.AdminEngine;
import com.example.planit.model.mongo.course.Course;
import com.example.planit.model.mongo.course.CoursesRepository;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.Constants;
import com.example.planit.utill.dto.*;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class AdminController {
    @Autowired
    CoursesRepository courseRepo;
    @Autowired
    UserRepository userRepo;

    public static Logger logger = LogManager.getLogger(AdminController.class);

    private AdminEngine adminEngine;

    @PostConstruct
    private void init() {
        this.adminEngine = new AdminEngine(courseRepo, userRepo);
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
    @GetMapping(value = "/admin/course")
    public ResponseEntity<DTOcoursesResponseToClient> getCourse(@RequestParam String courseId, @RequestParam String sub) {
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
        DTOusersResponseToController dtOcoursesResponseToController = adminEngine.getAllUsersFromDB(sub);

        return ResponseEntity.status(dtOcoursesResponseToController.getHttpStatus())
                .body(new DTOusersResponseToClient(dtOcoursesResponseToController.isSucceed(),
                        dtOcoursesResponseToController.getDetails(), dtOcoursesResponseToController.getUsers()));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping(value = "/admin/make-user-admin")
    public ResponseEntity<DTOstatus> makeUserAdmin(@RequestParam String sub, @RequestParam String userSubId) {
        DTOusersResponseToController dtOcoursesResponseToController = adminEngine.makeUserAdminInDB(sub, userSubId);

        return ResponseEntity.status(dtOcoursesResponseToController.getHttpStatus())
                .body(new DTOstatus(dtOcoursesResponseToController.isSucceed(),
                        dtOcoursesResponseToController.getDetails()));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping(value = "/admin/update-holidays")
    public ResponseEntity<DTOstatus> updateHolidays(@RequestParam String sub) {

        // for oshri to continue implementation
        return ResponseEntity.status(HttpStatus.OK).body(new DTOstatus(true, Constants.NO_PROBLEM));
    }

}


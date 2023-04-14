package com.example.planit.controller;

import com.example.planit.engine.AdminEngine;
import com.example.planit.model.mongo.course.Course;
import com.example.planit.model.mongo.course.CoursesRepository;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.dto.DTOcoursesResponseToClient;
import com.example.planit.utill.dto.DTOcoursesResponseToController;
import com.example.planit.utill.dto.DTOstatus;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class AdminController {
    @Autowired
    CoursesRepository courseRepo;
    @Autowired
    UserRepository userRepo;

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

    @PostMapping(value = "/admin/courses")
    public ResponseEntity<DTOstatus> addCourse(@RequestBody Course course, @RequestParam String sub) {
        DTOcoursesResponseToController dtOcoursesResponseToController = adminEngine.addCourseToDB(course, sub);

        return ResponseEntity.status(dtOcoursesResponseToController.getHttpStatus())
                .body(new DTOstatus(dtOcoursesResponseToController.isSucceed(),
                        dtOcoursesResponseToController.getDetails()));
    }

    @PutMapping(value = "/admin/courses")
    public ResponseEntity<DTOstatus> updateCourse(@RequestBody Course course, @RequestParam String sub) {
        DTOcoursesResponseToController dtOcoursesResponseToController = adminEngine.updateCourseInDB(course, sub);

        return ResponseEntity.status(dtOcoursesResponseToController.getHttpStatus())
                .body(new DTOstatus(dtOcoursesResponseToController.isSucceed(),
                        dtOcoursesResponseToController.getDetails()));
    }

}


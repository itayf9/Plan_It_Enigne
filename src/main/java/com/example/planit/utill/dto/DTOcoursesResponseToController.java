package com.example.planit.utill.dto;

import com.example.planit.model.mongo.course.Course;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class DTOcoursesResponseToController extends DTOstatus {
    private final HttpStatus httpStatus;
    private final List<Course> courses;

    public DTOcoursesResponseToController(boolean isSucceed, String details, HttpStatus httpStatus, List<Course> courses) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
        this.courses = courses;
    }

    public DTOcoursesResponseToController(boolean isSucceed, String details, HttpStatus httpStatus) {
        super(isSucceed, details);
        this.httpStatus = httpStatus;
        this.courses = new ArrayList<>();
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public List<Course> getCourses() {
        return courses;
    }
}

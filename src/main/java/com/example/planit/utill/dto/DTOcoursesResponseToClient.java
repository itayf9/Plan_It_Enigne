package com.example.planit.utill.dto;

import com.example.planit.model.mongo.course.Course;

import java.util.List;

public class DTOcoursesResponseToClient extends DTOstatus {
    List<Course> courses;

    public DTOcoursesResponseToClient(boolean isSucceed, String details, List<Course> courses) {
        super(isSucceed, details);
        this.courses = courses;
    }

    public List<Course> getCourses() {
        return courses;
    }
}

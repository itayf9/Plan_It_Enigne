package com.example.lamivhan.model.exam;

import com.example.lamivhan.model.mongo.course.Course;
import com.google.api.client.util.DateTime;

public class Exam {
    private Course course;
    private DateTime dateTime;

    public Exam(Course course, DateTime dateTime) {
        this.course = course;
        this.dateTime = dateTime;
    }

    public Course getCourse() {
        return course;
    }

    public DateTime getDateTime() {
        return dateTime;
    }
}

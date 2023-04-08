package com.example.planit.model.exam;

import com.example.planit.model.mongo.course.Course;
import com.google.api.client.util.DateTime;

import java.util.Objects;

public class Exam {
    private final Course course;
    private final DateTime dateTime;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return Objects.equals(course, exam.course) && Objects.equals(dateTime, exam.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(course, dateTime);
    }
}

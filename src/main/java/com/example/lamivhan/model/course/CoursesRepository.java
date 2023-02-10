package com.example.lamivhan.model.course;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CoursesRepository extends MongoRepository<Course, String> {

    Optional<Course> findCourseByCourseName(String courseName);
}

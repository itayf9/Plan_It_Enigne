package com.example.planit.model.mongo.course;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CoursesRepository extends MongoRepository<Course, String> {

    Optional<Course> findCourseByCourseName(String courseName);

    Optional<Course> findCourseById(String courseId);
}

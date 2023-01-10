package com.example.lamivhan.model.course;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CoursesRepository extends MongoRepository<Course, String> {

}

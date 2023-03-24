package com.example.planit.model.mongo.user;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findUserByEmail(String i_email);

    Optional<User> findUserBySubjectId(String i_subjectId);

}

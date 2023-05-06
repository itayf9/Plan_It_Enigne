package com.example.planit.model.mongo.holiday;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface HolidayRepository  extends MongoRepository<Holiday, String> {

}

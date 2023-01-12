package com.example.lamivhan.model.user;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
@Document("users")
public class User {
    @Field(name = "user_name")
    private String userName;
    @Field(name = "is_day_learner")
    private boolean isDayLearner;

    public User(String userName, boolean isDayLearner) {
        this.userName = userName;
        this.isDayLearner = isDayLearner;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isDayLearner() {
        return isDayLearner;
    }

    public void setDayLearner(boolean dayLearner) {
        isDayLearner = dayLearner;
    }
}

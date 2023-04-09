package com.example.planit.model.mongo.user;

import org.springframework.data.mongodb.core.mapping.Field;

public class Profile {

    @Field(name = "email")
    private final String email;

    @Field(name = "name")
    private final String name;

    @Field(name = "picture_url")
    private final String pictureUrl;

    public Profile(String email, String name, String pictureUrl) {
        this.email = email;
        this.name = name;
        this.pictureUrl = pictureUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }
}

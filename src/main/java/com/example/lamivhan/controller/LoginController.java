package com.example.lamivhan.controller;

import com.example.lamivhan.googleapis.AccessToken;
import com.example.lamivhan.model.preferences.Preferences;
import com.example.lamivhan.model.user.User;
import com.example.lamivhan.model.user.UserRepository;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Autowired
    private UserRepository userRepo;

    @PostMapping(value = "/login",
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public void login(@RequestBody AccessToken accessToken) {

        // login will be directly through Google Oauth, so I don't think there is much to do here

        // maybe redirect to profile page or home page - could be all implemented in frontend stuff
    }

    @PostMapping(value = "/sign-up")
    public ResponseEntity<String> signUp(@RequestBody String email) {

        // check if user is new and not already exist in DB
        if (userRepo.findUserByEmail(email).isEmpty()) {
            // 1. create Java user-object
            // 2. save user to DB with userRepo
            userRepo.save(new User(email));
            return ResponseEntity.status(HttpStatus.OK).body("User saved successfully");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exist please log-in");
    }

    @GetMapping(value = "/profile", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public User getUserPreferencesFromDB(@RequestBody String email) {
        // assuming user exist and email will be found when this endpoint will be called.
        return userRepo.findUserByEmail(email).get();
    }

    @GetMapping(value = "/logout")
    public void logout() {

        // 1. ???
    }

}
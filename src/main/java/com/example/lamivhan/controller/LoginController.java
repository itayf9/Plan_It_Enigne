package com.example.lamivhan.controller;

import com.example.lamivhan.model.user.User;
import com.example.lamivhan.model.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class LoginController {

    @Autowired
    UserRepository userRepo;

    /**
     * login endpoint
     */
    @PostMapping(value = "/login")
    public void login() {

        // login will be directly through Google Oauth, so I don't think there is much to do here

        // maybe redirect to profile page or home page - could be all implemented in frontend stuff
    }

    /**
     * @param email email of the user
     * @return response entity with status message
     */
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

    /**
     * @param email email of the user
     * @return User object with preferences.
     */
    @GetMapping(value = "/profile", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public User getUserPreferencesFromDB(@RequestBody String email) throws Exception {

        // assuming user exist and email will be found when this endpoint will be called.
        Optional<User> maybeUser = userRepo.findUserByEmail(email);
        if (maybeUser.isPresent()) {
            return maybeUser.get();
        } else {
            throw new Exception("No User Found with this email");
        }
    }

    @GetMapping(value = "/logout")
    public void logout() {

        // 1. ???
    }

}

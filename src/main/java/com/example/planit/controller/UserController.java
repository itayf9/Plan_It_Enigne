package com.example.planit.controller;

import com.example.planit.engine.UserEngine;
import com.example.planit.model.mongo.user.User;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.model.mongo.user.preferences.Preferences;
import com.example.planit.utill.Constants;
import com.example.planit.utill.dto.DTOloginResponseToClient;
import com.example.planit.utill.dto.DTOstatus;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class UserController {

    @Autowired
    private Environment env;

    @Autowired
    UserRepository userRepo;

    private UserEngine userEngine;

    @PostConstruct
    private void init() {
        // initialize CalendarEngine
        this.userEngine = new UserEngine(userRepo, env);
    }

    /**
     * login endpoint : this endpoint will check if user signed up before pressing the login
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/login")
    public ResponseEntity<DTOloginResponseToClient> signUpOrLogin(@RequestParam(value = "code") String authCode) {

        // decode auth  (e.g. %2F to /)
        authCode = URLDecoder.decode(authCode, StandardCharsets.UTF_8);

        GoogleTokenResponse googleTokenResponse;

        //
        try {
            googleTokenResponse = userEngine.getGoogleTokensFromAuthCode(authCode);

            // check if user subjectId exist in the DB
            String subjectId = googleTokenResponse.parseIdToken().getPayload().getSubject();

            if (userRepo.findUserBySubjectID(subjectId).isEmpty()) {
                // the user does not exist in DB, refers to sign up
                String subjectID = userEngine.createNewUserAndSaveToDB(googleTokenResponse);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new DTOloginResponseToClient(true, Constants.REGISTER, subjectID));

            } else {
                // the user already exists in the DB, refers to sign in
                String subjectID = userEngine.updateAuthorizationTokens(googleTokenResponse);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new DTOloginResponseToClient(true, Constants.LOGIN, subjectID));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
            // return http response "the auth code is not valid",
        }
    }

    /**
     * get the user Object from the DB to later display Profile information of the user
     *
     * @param sub the sub value of the user
     * @return User object with preferences.
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value = "/profile", produces = {MediaType.APPLICATION_JSON_VALUE})
    public User getUserPreferencesFromDB(@RequestParam String sub) throws Exception {

        // assuming user exist and subjectID will be found when this endpoint will be called.
        Optional<User> maybeUser = userRepo.findUserBySubjectID(sub);

        if (maybeUser.isPresent()) {
            return maybeUser.get();
        } else {
            throw new Exception("No User Found with this email");
        }
    }

    /**
     *
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/profile", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<DTOstatus> updateUserPreferencesInDB(@RequestBody Preferences preferences, @RequestParam String sub) {

        // assuming user exist and subjectID will be found when this endpoint will be called.
        Optional<User> maybeUser = userRepo.findUserBySubjectID(sub);

        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            user.setUserPreferences(preferences);

            userRepo.save(user);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new DTOstatus(true, Constants.NO_PROBLEM));

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new DTOstatus(false, Constants.ERROR_UNAUTHORIZED_USER));
        }
    }
}

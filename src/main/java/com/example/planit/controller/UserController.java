package com.example.planit.controller;

import com.example.planit.engine.UserEngine;
import com.example.planit.model.mongo.user.User;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.model.mongo.user.preferences.Preferences;
import com.example.planit.utill.Constants;
import com.example.planit.utill.dto.DTOloginResponseToClient;
import com.example.planit.utill.dto.DTOstatus;
import com.example.planit.utill.dto.DTOuserClientRepresentation;
import com.example.planit.utill.exception.UnauthorizedUserException;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Optional;

import static com.example.planit.model.mongo.user.UserClientRepresentation.buildUserClientRepresentationFromUser;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class UserController {

    private static final Logger logger = LogManager.getLogger(UserController.class);

    @Autowired
    private Environment env;

    @Autowired
    UserRepository userRepo;

    private UserEngine userEngine;

    @PostConstruct
    private void init() {
        this.userEngine = new UserEngine(userRepo, env);
    }

    /**
     * login endpoint : this endpoint will check if user signed up before pressing the login
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/login")
    public ResponseEntity<DTOloginResponseToClient> signUpOrLogin(@RequestParam(value = "code") String authCode) {

        long s = System.currentTimeMillis();
        logger.info(MessageFormat.format("New login: user has requested POST /login with params: code={0}", authCode));

        //
        try {

            // decode auth  (e.g. %2F to /)
            authCode = URLDecoder.decode(authCode, StandardCharsets.UTF_8);

            GoogleTokenResponse googleTokenResponse;


            googleTokenResponse = userEngine.getGoogleTokensFromAuthCode(authCode);

            // check if user subjectId exist in the DB
            String subjectId = googleTokenResponse.parseIdToken().getPayload().getSubject();

            if (userRepo.findUserBySubjectID(subjectId).isEmpty()) {
                // the user does not exist in DB, refers to sign up
                String subjectID = userEngine.createNewUserAndSaveToDB(googleTokenResponse);
                boolean isAdmin = userEngine.isTheUserWithThisSubjectIdAnAdmin(subjectID);

                long t = System.currentTimeMillis();
                long res = t - s;
                logger.info(MessageFormat.format("User {0}: was successfully signed up. time {1} ms", subjectID, res));

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new DTOloginResponseToClient(true, Constants.REGISTER, subjectID, isAdmin));

            } else {
                // the user already exists in the DB, refers to sign in
                String subjectID = userEngine.updateAuthorizationTokens(googleTokenResponse);
                boolean isAdmin = userEngine.isTheUserWithThisSubjectIdAnAdmin(subjectID);

                long t = System.currentTimeMillis();
                long res = t - s;
                logger.info(MessageFormat.format("User {0}: was successfully signed in. time {1} ms", subjectID, res));

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new DTOloginResponseToClient(true, Constants.LOGIN, subjectID, isAdmin));
            }

        } catch (UnauthorizedUserException e) {
            // the user has become unauthorized (not found in the DB)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new DTOloginResponseToClient(false, Constants.ERROR_UNAUTHORIZED_USER, e.getSubjectID(), false));
        } catch (IOException e) {
            // return http response "the auth code is not valid",
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DTOloginResponseToClient(false, Constants.ERROR_FROM_GOOGLE_API_EXECUTE, null, false));
        } catch (IllegalArgumentException e) {
            // when the authCode is not in the right format
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new DTOloginResponseToClient(false, Constants.ERROR_ILLEGAL_CHARACTERS_IN_AUTH_CODE, null, false));
        } catch (Exception e) {
            // an unknown error had happened
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DTOloginResponseToClient(false, Constants.ERROR_DEFAULT, null, false));
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
    public ResponseEntity<DTOuserClientRepresentation> getUserPreferencesFromDB(@RequestParam String sub) {

        long s = System.currentTimeMillis();
        logger.info(MessageFormat.format("User {0}: has requested GET /profile with params: sub={0}", sub));

        // assuming user exist and subjectID will be found when this endpoint will be called.
        Optional<User> maybeUser = userRepo.findUserBySubjectID(sub);

        long t = System.currentTimeMillis();
        long res = t - s;
        logger.info(MessageFormat.format("User {0}: profile time is {1} ms", sub, res));

        if (maybeUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new DTOuserClientRepresentation(true, Constants.NO_PROBLEM,
                            buildUserClientRepresentationFromUser(maybeUser.get())));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new DTOuserClientRepresentation(false, Constants.ERROR_UNAUTHORIZED_USER));
        }
    }

    /**
     *
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/profile", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<DTOstatus> updateUserPreferencesInDB(@RequestBody Preferences preferences, @RequestParam String sub) {

        long s = System.currentTimeMillis();
        logger.info(MessageFormat.format("User {0}: has requested POST /profile with params: sub={0}, preferences={1}", sub, preferences.toString()));

        // assuming user exist and subjectID will be found when this endpoint will be called.
        Optional<User> maybeUser = userRepo.findUserBySubjectID(sub);

        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            user.setUserPreferences(preferences);
            user.setCompletedFirstSetup(true); // assume that when ever the user gets here that means he completed the first setup
            userRepo.save(user);

            long t = System.currentTimeMillis();
            long res = t - s;
            logger.info(MessageFormat.format("User {0}: successfully saved preferences in DB. profile time is {1} ms", sub, res));

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new DTOstatus(true, Constants.NO_PROBLEM));

        } else {
            long t = System.currentTimeMillis();
            long res = t - s;
            logger.info(MessageFormat.format("User {0}: could not save preferences in DB. profile time is {1} ms", sub, res));

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new DTOstatus(false, Constants.ERROR_UNAUTHORIZED_USER));
        }
    }
}

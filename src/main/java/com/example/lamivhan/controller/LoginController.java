package com.example.lamivhan.controller;

import com.example.lamivhan.model.mongo.user.User;
import com.example.lamivhan.model.mongo.user.UserRepository;
import com.example.lamivhan.utill.dto.DTOtokens;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

@RestController
public class LoginController {

    @Autowired
    private Environment env;

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
     * register a user end-point
     * @param code auth-code of the user
     * @return response entity with status message
     */
    @PostMapping(value = "/sign-up")
    public ResponseEntity<String> signUp(@RequestBody String code) throws IOException {

        DTOtokens tokens = getEmailAndTokensFromAuthCode(code);

        String email = tokens.getUserEmail();

        // check if user is new and not already exist in DB
        if (userRepo.findUserByEmail(email).isEmpty()) {
            // 1. create Java user-object
            // 2. save user to DB with userRepo
            userRepo.save(new User(email, tokens.getRefreshToken(), tokens.getAccessToken()));
            return ResponseEntity.status(HttpStatus.OK).body("User saved successfully");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exist please log-in");
    }

    /**
     * extract the user's email and access tokens, from the auth code sent by the front-end.
     * @param code Auth-Code from Frontend OAuth process.
     * @return DTO contains access tokens, Refresh token & user Email
     * @throws IOException exception
     */
    private DTOtokens getEmailAndTokensFromAuthCode(String code) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        code = URLDecoder.decode(code, StandardCharsets.UTF_8);

        String REDIRECT_URI = "http://localhost:8083/callback";

        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                jsonFactory,
                Objects.requireNonNull(env.getProperty("spring.security.oauth2.client.registration.google.client-id")),
                env.getProperty("spring.security.oauth2.client.registration.google.client-secret"),
                code,
                REDIRECT_URI
        ).execute();

        String email = tokenResponse.parseIdToken().getPayload().getEmail();

        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();
        return new DTOtokens(accessToken, refreshToken, email);
    }

    /**
     * get the user Object from the DB to later display Profile information of the user
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

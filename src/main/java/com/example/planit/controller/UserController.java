package com.example.planit.controller;

import com.example.planit.model.mongo.user.User;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.Constants;
import com.example.planit.utill.dto.DTOloginResponseToClient;
import com.example.planit.utill.dto.DTOtokens;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")

@RestController
public class UserController {

    @Autowired
    private Environment env;

    @Autowired
    UserRepository userRepo;

    /**
     * login endpoint : this endpoint will check if user signed up before pressing the login
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/login")
    public ResponseEntity<DTOloginResponseToClient> signUpOrLogin(@RequestParam String authCode) {

        // decode auth  (e.g. %2F to /)
        authCode = URLDecoder.decode(authCode, StandardCharsets.UTF_8);

        GoogleTokenResponse googleTokenResponse = null;

        //
        try {
            googleTokenResponse = getGoogleTokensFromAuthCode(authCode);

            // check if user subjectId exist in the DB
            String subjectId = googleTokenResponse.parseIdToken().getPayload().getSubject();

            if (userRepo.findUserBySubjectId(subjectId).isEmpty()) {
                // the user does not exist in DB, refers to sign up
                String subjectID = createNewUserAndSaveToDB(googleTokenResponse);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new DTOloginResponseToClient(true, Constants.REGISTER, subjectID));

            } else {
                // the user already exists in the DB, refers to sign in
                String subjectID = updateAuthorizationTokens(googleTokenResponse);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new DTOloginResponseToClient(true, Constants.LOGIN, subjectID));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
            // return http response "the auth code is not valid",
        }



        /*

        - addUserToDB ( name, email, picture, refreshToken, sub )

        - verifyIDToken ?

        - signUpOrIn ( authCode ) {
            // decode to get IDToken
            // ? verify IDToken
            // ?  if valid:
            //      fetch sub
            //      if user no in DB:
            //          fetch user info
            //          save access and refresh in DB
            //          create new user and save in DB
            //          return 201 register the user (with the subject id)
            //      else:
            //          save access and refresh in DB
            //          return 200 the user is in the db and update the tokens (with subject id)
        }

        - remove login endpoint

        - validateAccessToken : add exception handlers for refreshAccessToken

         */
    }

    /**
     * update the access and refresh token in the DB for the user we get by the subject id in the googleTokenResponse
     *
     * @param googleTokenResponse google object that contain access and refresh token, subject id.
     * @throws IOException
     * @throws NoSuchElementException
     */
    private String updateAuthorizationTokens(GoogleTokenResponse googleTokenResponse) throws IOException, NoSuchElementException {
        DTOtokens tokens = getSubjectIdAndTokens(googleTokenResponse);

        // get the
        String subjectId = tokens.getSubjectId();
        // get the user by the subject id
        Optional<User> maybeUser = userRepo.findUserBySubjectId(subjectId);
        // get the user for update the tokens.
        User user = maybeUser.get();


        // get the access and refresh token form the
        String accessToken = tokens.getAccessToken();
        String refreshToken = tokens.getRefreshToken();

        // we decrease the 100 seconds to make sure expire date will be valid even with busy network traffic
        long expireTimeInMilliseconds = tokens.getExpireTimeInMilliseconds();

        // update the access and refresh tokens of the user.
        user.setAccessToken(accessToken);
        user.setExpireTimeInMilliseconds(expireTimeInMilliseconds);
        user.setRefreshToken(refreshToken);
        userRepo.save(user);
        return user.getSubjectId();
    }

    /**
     * creates a new user using the auth code and ID token from Google.
     * extracts user's profile information.
     * extracts user's identifier (sub).
     * extracts user's authentication information (access, refresh, expire time).
     * then, adds th user to the DB
     *
     * @param googleTokenResponse a {@link GoogleTokenResponse} to retrieve the information from
     * @throws IOException
     */
    private String createNewUserAndSaveToDB(GoogleTokenResponse googleTokenResponse) throws IOException {

        GoogleIdToken idToken = googleTokenResponse.parseIdToken();

        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        DTOtokens tokens = getSubjectIdAndTokens(googleTokenResponse);

        String subjectId = tokens.getSubjectId();
        String accessToken = tokens.getAccessToken();
        long expireTimeInMilliseconds = tokens.getExpireTimeInMilliseconds();
        String refreshToken = tokens.getRefreshToken();

        userRepo.save(new User(subjectId, email, name, pictureUrl, accessToken, expireTimeInMilliseconds, refreshToken));
        return subjectId;
    }

    /**
     * extract the access, refresh tokens and subject id and add the expireTimeInMilliseconds
     *
     * @param googleTokenResponse
     * @return DTOtokens that contain the Subject id, access and refresh tokens and the expires time to the access token.
     * @throws IOException
     */
    private DTOtokens getSubjectIdAndTokens(GoogleTokenResponse googleTokenResponse) throws IOException {
        String subjectId = googleTokenResponse.parseIdToken().getPayload().getSubject();

        // get the access and refresh token form the
        String accessToken = googleTokenResponse.getAccessToken();
        String refreshToken = googleTokenResponse.getRefreshToken();

        // we decrease the 100 seconds to make sure expire date will be valid even with busy network traffic
        long expireTimeInMilliseconds = Instant.now().plusMillis(((googleTokenResponse.getExpiresInSeconds() - 100) * 1000)).toEpochMilli();

        return new DTOtokens(accessToken, expireTimeInMilliseconds, refreshToken, subjectId);
    }


    /**
     * register a user end-point
     *
     * @param authCode auth-code of the user
     * @return response entity with status message
     */
   /* @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/sign-up")
    public ResponseEntity<String> signUp(@RequestParam String authCode) throws IOException {

//        authCode = URLDecoder.decode(authCode, StandardCharsets.UTF_8);
//        // DTOtokens tokens = getGoogleTokensFromAuthCode(authCode);
//
//        String email = tokens.getUserEmail();
//        String subjectId = tokens.getSubjectId();
////
////
//
//        // check if user is new and not already exist in DB
//        if (userRepo.findUserBySubjectId(subjectId).isEmpty()) {
//            // 1. create Java user-object
//            // 2. save user to DB with userRepo
//            userRepo.save(new User(email, tokens.getAccessToken(), tokens.getExpireTimeInMilliseconds()
//                    , tokens.getRefreshToken(), tokens.getSubjectId()));
//            return ResponseEntity.status(HttpStatus.OK).body("User saved successfully");
//
//        }
//        return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exist please log-in");
    }*/

    /**
     * extract the user's email and access tokens, from the auth code sent by the front-end.
     *
     * @param code Auth-Code from Frontend OAuth process.
     * @return DTO contains access tokens, Refresh token & user Email
     * @throws IOException exception
     */
    private GoogleTokenResponse getGoogleTokensFromAuthCode(String code) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        String REDIRECT_URI = "http://localhost:3000";
        String CLIENT_ID = Objects.requireNonNull(env.getProperty("spring.security.oauth2.client.registration.google.client-id"));
        String CLIENT_SECRET = env.getProperty("spring.security.oauth2.client.registration.google.client-secret");

        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                jsonFactory,
                CLIENT_ID,
                CLIENT_SECRET,
                code,
                REDIRECT_URI
        ).execute();

        /*String email = tokenResponse.parseIdToken().getPayload().getEmail();

        String accessToken = tokenResponse.getAccessToken();

        // we decrease the 100 seconds to make sure expire date will be valid even with busy network traffic
        long expireTimeInMilliseconds = Instant.now().plusMillis(((tokenResponse.getExpiresInSeconds() - 100) * 1000)).toEpochMilli();

        String refreshToken = tokenResponse.getRefreshToken();
        return new DTOtokens(accessToken, expireTimeInMilliseconds, refreshToken, email);
    }

    /**
     * get the user Object from the DB to later display Profile information of the user
     *
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

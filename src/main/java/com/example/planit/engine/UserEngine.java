package com.example.planit.engine;

import com.example.planit.model.mongo.user.User;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.dto.DTOtokens;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

public class UserEngine {

    private final UserRepository userRepo;
    private final Environment env;

    public UserEngine(UserRepository userRepo, Environment env) {
        this.userRepo = userRepo;
        this.env = env;
    }

    /**
     * update the access and refresh token in the DB for the user we get by the subject id in the googleTokenResponse
     *
     * @param googleTokenResponse google object that contain access and refresh token, subject id.
     * @throws IOException            IOException
     * @throws NoSuchElementException NoSuchElementException
     */
    public String updateAuthorizationTokens(GoogleTokenResponse googleTokenResponse) throws IOException, NoSuchElementException {
        DTOtokens tokens = getSubjectIdAndTokens(googleTokenResponse);

        // get the
        String subjectID = tokens.getSubjectId();
        // get the user by the subject id
        Optional<User> maybeUser = userRepo.findUserBySubjectID(subjectID);
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
     * @throws IOException IOException
     */
    public String createNewUserAndSaveToDB(GoogleTokenResponse googleTokenResponse) throws IOException {

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
     * @param googleTokenResponse Google object containing access_token, refresh_token and id token
     * @return DTOtokens that contain the Subject id, access and refresh tokens and the expires time to the access token.
     * @throws IOException IOException
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
     * extract the user's email and access tokens, from the auth code sent by the front-end.
     *
     * @param code Auth-Code from Frontend OAuth process.
     * @return DTO contains access tokens, Refresh token & user Email
     * @throws IOException exception
     */
    public GoogleTokenResponse getGoogleTokensFromAuthCode(String code) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        String REDIRECT_URI = "http://localhost:3000";
        String CLIENT_ID = Objects.requireNonNull(env.getProperty("spring.security.oauth2.client.registration.google.client-id"));
        String CLIENT_SECRET = env.getProperty("spring.security.oauth2.client.registration.google.client-secret");

        return new GoogleAuthorizationCodeTokenRequest(
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

        String subjectId = tokenResponse.parseIdToken().getPayload().getSubject();

        return new DTOtokens(accessToken, expireTimeInMilliseconds, refreshToken, email, subjectId);*/
    }
}

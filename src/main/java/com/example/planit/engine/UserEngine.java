package com.example.planit.engine;

import com.example.planit.model.mongo.user.User;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.Constants;
import com.example.planit.utill.dto.DTOtokens;
import com.example.planit.utill.exception.UnauthorizedUserException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.example.planit.utill.Constants.PLAN_IT_WEB_PRODUCTION_URI;

@Service
public class UserEngine {

    @Autowired
    private UserRepository userRepo;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String CLIENT_ID;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    String CLIENT_SECRET;

    public static Logger logger = LogManager.getLogger(UserEngine.class);

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
        user.getAuth().setAccessToken(accessToken);
        user.getAuth().setExpireTimeInMilliseconds(expireTimeInMilliseconds);
        user.getAuth().setRefreshToken(refreshToken);
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
     * @param isDev is the Front-End sent request in Developer mode
     * @return DTO contains access tokens, Refresh token & user Email
     * @throws IOException exception
     */
    public GoogleTokenResponse getGoogleTokensFromAuthCode(String code, boolean isDev) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        String REDIRECT_URI = isDev ? "http://localhost:3000" : PLAN_IT_WEB_PRODUCTION_URI;


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

    /**
     * checks in the DB if the given subjectID represents a registered user.
     * then checks if the user is an admin.
     *
     * @param subjectID a string represents the subjectID of a user.
     * @return rue if the user is registered and an admin. false if the user is registers and not an admin. null if the user is not registered
     * @throws UnauthorizedUserException if the user's subjectID does not appear in the DB
     */
    public boolean isTheUserWithThisSubjectIdAnAdmin(String subjectID) throws UnauthorizedUserException {

        Optional<User> maybeUser = userRepo.findUserBySubjectID(subjectID);

        if (maybeUser.isPresent()) {
            return maybeUser.get().isAdmin();
        } else {
            throw new UnauthorizedUserException(subjectID);
        }
    }
}

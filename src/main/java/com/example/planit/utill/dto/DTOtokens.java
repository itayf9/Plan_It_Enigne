package com.example.planit.utill.dto;

public class DTOtokens {

    private final String AccessToken;

    private final long expireTimeInMilliseconds;

    private final String RefreshToken;

    private final String SubjectId;


    public DTOtokens(String accessToken, long expireTimeInMilliseconds, String refreshToken, String subjectId) {
        AccessToken = accessToken;
        RefreshToken = refreshToken;
        this.expireTimeInMilliseconds = expireTimeInMilliseconds;
        this.SubjectId = subjectId;
    }

    public String getAccessToken() {
        return AccessToken;
    }

    public String getRefreshToken() {
        return RefreshToken;
    }

    public long getExpireTimeInMilliseconds() {
        return expireTimeInMilliseconds;
    }


    public String getSubjectId() {
        return SubjectId;
    }
}

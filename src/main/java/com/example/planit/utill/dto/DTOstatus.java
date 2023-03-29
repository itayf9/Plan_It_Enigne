package com.example.planit.utill.dto;

public class DTOstatus {

    private final boolean isSucceed;
    private final String details;

    public DTOstatus(boolean isSucceed, String details) {
        this.isSucceed = isSucceed;
        this.details = details;
    }

    public boolean isSucceed() {
        return isSucceed;
    }

    public String getDetails() {
        return details;
    }
}

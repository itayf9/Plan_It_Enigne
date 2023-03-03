package com.example.lamivhan.utill.dto;

public class DTOstatus {

    private boolean isSucceed;
    private String details;

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

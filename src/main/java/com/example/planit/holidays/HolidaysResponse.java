package com.example.planit.holidays;

public class HolidaysResponse {
    private Meta meta;
    private Response response;

    public HolidaysResponse() {
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
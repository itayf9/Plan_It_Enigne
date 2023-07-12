package com.example.planit.engine;

import com.example.planit.holidays.Holiday;
import com.example.planit.holidays.HolidaysResponse;
import com.google.gson.Gson;
import com.squareup.okhttp.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
@Service
public class HolidaysEngine {

    public static Logger logger = LogManager.getLogger(HolidaysEngine.class);

    public static Gson gson = new Gson();
    private static final String UrlRequest = "https://calendarific.com/api/v2/holidays";

    /**
     * get api key, country, year and return a set of holidays days for the current country in the requested year.
     *
     * @param holidaysApiKey api key form "calendarific"
     * @param country        country (ISO-3166) from "calendarific"
     * @param year           the requested year
     * @return set of string that present the dates of the holidays.
     */
    public Set<com.example.planit.model.mongo.holiday.Holiday> getDatesOfHolidays(String holidaysApiKey, String country, int year) throws IOException {
        Set<com.example.planit.model.mongo.holiday.Holiday> allHolidays = new HashSet<>();

        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(UrlRequest).newBuilder();
        urlBuilder.addQueryParameter("api_key", holidaysApiKey);
        urlBuilder.addQueryParameter("country", country);
        urlBuilder.addQueryParameter("year", Integer.toString(year));
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder().url(url).build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() != 200) {
            throw new RuntimeException("code: " + response.code());
        } else {

            // parse the response to holidaysResponse object
            HolidaysResponse holidaysResponse = gson.fromJson(response.body().string(), HolidaysResponse.class);

            // extract the holidays in array to convert them to map
            Holiday[] holidaysArray = holidaysResponse.getResponse().getHolidays();

            for (Holiday holiday : holidaysArray) {

                // add the date of the holidays to set
                allHolidays.add(new com.example.planit.model.mongo.holiday.Holiday(holiday.getName(), holiday.getDate().getIso()));
            }
        }
        return allHolidays;
    }
}

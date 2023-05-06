package com.example.planit.engine;

import com.example.planit.holidays.Holiday;
import com.example.planit.holidays.HolidaysResponse;
import com.google.api.services.calendar.model.Event;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HolidaysEngine {

    public static Logger logger = LogManager.getLogger(HolidaysEngine.class);

    public static Gson gson = new Gson();

    //static final String apiKey = env.get //"c2c659ee-3ac3-4699-b5fa-ff45ea405d7e";
    private static final String UrlRequest = "https://calendarific.com/api/v2/holidays";

    /**
     * get api key, country, year and return a set of holidays days for the current country in the requested year.
     *
     * @param holidaysApiKey api key form "calendarific"
     * @param country        country (ISO-3166) from "calendarific"
     * @param year           the requested year
     * @return set of string that present the dates of the holidays.
     */
    public static Set<com.example.planit.model.mongo.holiday.Holiday> getDatesOfHolidays(String holidaysApiKey, String country, int year) {

        Unirest.setTimeouts(0, 0);

        Set<com.example.planit.model.mongo.holiday.Holiday> allHolidays = new HashSet<>();

        // create the url with query parameters
        try {
            URIBuilder uriBuilder = new URIBuilder("https://calendarific.com/api/v2/holidays");
            uriBuilder.addParameter("api_key", holidaysApiKey);
            uriBuilder.addParameter("country", country);
            uriBuilder.addParameter("year", Integer.toString(year));
            uriBuilder.build();

            // send get request to "calendarific" server with the url that require to get all jews holidays
            HttpResponse<String> response = Unirest.get(uriBuilder.toString()).asString();

            if (response.getStatus() != 200) {
                throw new RuntimeException("code: " + response.getStatus());
            } else {
                // parse the response to holidaysResponse object
                HolidaysResponse holidaysResponse = gson.fromJson(response.getBody(), HolidaysResponse.class);

                // extract the holidays in array to convert them to map
                Holiday[] holidaysArray = holidaysResponse.getResponse().getHolidays();

                for (Holiday holiday : holidaysArray) {

                    // add the date of the holidays to set
                    allHolidays.add(new com.example.planit.model.mongo.holiday.Holiday(holiday.getName(), holiday.getDate().getIso()));
                }
            }

        } catch (UnirestException | URISyntaxException e) {
            throw new RuntimeException();
        }
        return allHolidays;
    }

    /**
     * get the country and the year and return in url the full url to get request form "calendarific" server.
     *
     * @param country the country we want the holidays from (e.g. il is all the jewish and national holidays in israel)
     * @param year    the year we want the date of the holidays
     * @return string that return contain the full url to get request
     */
    public static String createUrlForCurrentCountryAndYear(String holidaysApiKey, String country, int year) {
        return UrlRequest + "?" + "api_key=" + holidaysApiKey + "&country=" + country + "&year=" + year;
    }


}

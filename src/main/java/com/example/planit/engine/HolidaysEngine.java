package com.example.planit.engine;

import com.example.planit.holidays.Holiday;
import com.example.planit.holidays.HolidaysResponse;
import com.google.api.services.calendar.model.Event;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HolidaysEngine {

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
    public static Set<String> getDatesOfHolidays(String holidaysApiKey, String country, int year) {

        Unirest.setTimeouts(0, 0);

        String url = "";

        Set<String> allHolidays = new HashSet<>();
        // create the url with query parameters
        url = createUrlForCurrentCountryAndYear(holidaysApiKey, country, year);

        try {
            // send get request to "calendarific" server with the url that require to get all jews holidays
            HttpResponse<String> response = Unirest.get(url).asString();

            // parse the response to holidaysResponse object
            HolidaysResponse holidaysResponse = gson.fromJson(response.getBody(), HolidaysResponse.class);

            // check if we got 200 in code response
            if (holidaysResponse.getMeta().getCode() != 200) {
                // if not we throw exceptions with the code we got
                throw new RuntimeException("code: " + holidaysResponse.getMeta().getCode());
            }

            // extract the holidays in array to convert them to map
            Holiday[] holidaysArray = holidaysResponse.getResponse().getHolidays();

            for (Holiday holiday : holidaysArray) {

                // add the date of the holidays to set
                allHolidays.add(holiday.getDate().getIso());
            }

        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
        return allHolidays;
    }

    public static List<Event> handleHolidaysInFullDaysEvents(List<Event> fullDayEvents, List<Event> events
            , boolean isStudyOnHolyDays, Set<String> holidaysDatesCurrentYear, Set<String> holidaysDatesNextYear) {
        List<Event> copyOfFullDayEvents = new ArrayList<>(fullDayEvents);
        // scan through the list and check if an event is a holiday.
        for (Event fullDayEvent : fullDayEvents) {
            if (holidaysDatesCurrentYear.contains(fullDayEvent.getStart().getDate().toStringRfc3339())
                    || holidaysDatesNextYear.contains(fullDayEvent.getStart().getDate().toStringRfc3339())) {

                // check if user want to study on holidays
                if (isStudyOnHolyDays) {

                    // remove the holiday from the list of events
                    events.remove(fullDayEvent);
                }

                // remove the event from the copy of list of fullDayEvents and the events list
                copyOfFullDayEvents.remove(fullDayEvent);
            }
        }

        return copyOfFullDayEvents;
    }

    /**
     * get the country and the year and return in url the full url to get request form "calendarific" server.
     *
     * @param country the country we want the holidays from (e.g. il is all the jewish and national holidays in israel)
     * @param year    the year we want the date of the holidays
     * @return string that return contain the full url to get request
     */
    private static String createUrlForCurrentCountryAndYear(String holidaysApiKey, String country, int year) {
        return UrlRequest + "?" + "api_key=" + holidaysApiKey + "&country=" + country + "&year=" + year;
    }


}

package com.example.lamivhan;

import com.example.lamivhan.utill.Constants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@SpringBootApplication
public class LamivhanApplication {


    public static void main(String[] args) {

        Instant i = Instant.now();
        long l = i.toEpochMilli();

        //Instant b = i.plusSeconds(30 * 24 * 60 * 60);

        ZonedDateTime z1 = i.atZone(ZoneId.of(Constants.ISRAEL_TIME_ZONE));
        ZonedDateTime z2 = i.atZone(ZoneId.of("America/New_York"));
        ZonedDateTime z4 = i.atZone(ZoneId.of("Europe/Budapest"));

        //ZonedDateTime z3 = b.atZone(ZoneId.of(Constants.ISRAEL_TIME_ZONE));
        Instant newInstant = z1.toInstant();
        System.out.println("pure instant:                                    " + i);
        System.out.println("with time zone israel:                           " + z1);
        System.out.println("with time zone usa:                              " + z2);
        System.out.println("with time zone budapest:                         " + z4);
        System.out.println("back to instant:                                 " + newInstant);
        //System.out.println("+30 days with time zone israel:                  " + z3);

        SpringApplication.run(LamivhanApplication.class, args);
    }

}
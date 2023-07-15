package com.example.planit;

import com.example.planit.controller.CalendarController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
public class PlanITApplication implements CommandLineRunner {

    @Autowired
    private ApplicationContext context;

    public static Logger logger = LogManager.getLogger(CalendarController.class);
    @Override
    public void run(String... args) throws Exception {
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            logger.info("bean: " + beanName);
        }
        logger.info("bean count " + beanNames.length);
    }

    public static void main(String[] args) {
        SpringApplication.run(PlanITApplication.class, args);
    }
}
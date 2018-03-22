package com.credits;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@PropertySource("application.properties")
public class App {
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String... args) {
        logger.info("Contract executor is starting...");
        ApplicationContext context =
            new AnnotationConfigApplicationContext(App.class);
    }
}

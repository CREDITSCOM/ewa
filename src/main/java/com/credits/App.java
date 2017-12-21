package com.credits;

import com.credits.exception.ContractExecutorException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static void main(String... args) throws ContractExecutorException {
        SpringApplication.run(App.class, args);
    }
}

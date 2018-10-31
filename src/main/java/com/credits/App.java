package com.credits;

import com.credits.thrift.ContractExecutorServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String... args) {
        logger.info("Contract executor is starting...");
        new ContractExecutorServer();

    }
}

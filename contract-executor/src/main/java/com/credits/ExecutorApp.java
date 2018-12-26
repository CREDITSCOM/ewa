package com.credits;

import com.credits.thrift.ContractExecutorServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ExecutorApp {
    private final static Logger logger = LoggerFactory.getLogger(ExecutorApp.class);

    @Inject
    ApplicationProperties property;

    public static void main(String... args) {
        logger.info("Contract executor is starting...");
        ContractExecutorServer contractExecutorServer = new ContractExecutorServer();

    }
}

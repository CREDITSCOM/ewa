package com.credits;

import com.credits.thrift.ContractExecutorServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Properties;

public class ExecutorApp {
    private final static Logger logger = LoggerFactory.getLogger(ExecutorApp.class);

    public static void main(String... args) {
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream("settings.properties");
            properties.load(fis);
            String publicKey = properties.getProperty("contract.executor.public.key");
            System.setProperty(publicKey, properties.getProperty("contract.executor.private.key"));
        } catch (Throwable e) {
            throw new RuntimeException("can't load propertyFile", e);
        }

        logger.info("Contract executor is starting...");
        new ContractExecutorServer();

    }
}

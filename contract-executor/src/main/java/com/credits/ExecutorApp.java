package com.credits;

import com.credits.ioc.CEServerModule;
import com.credits.thrift.ContractExecutorServer;
import dagger.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

public class ExecutorApp {
    private final static Logger logger = LoggerFactory.getLogger(ExecutorApp.class);

    @Singleton
    @Component(modules = CEServerModule.class)
    interface ContractExecutorServerBuilder{
       ContractExecutorServer getContractExecutorServer();
    }


    public static void main(String... args) {
        logger.info("Contract executor is starting...");
        var CEServer = DaggerExecutorApp_ContractExecutorServerBuilder.builder().build().getContractExecutorServer();
        CEServer.start();
    }
}

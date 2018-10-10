package com.credits.thrift;

import com.credits.ApplicationProperties;
import com.credits.thrift.generated.ContractExecutor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.credits.ioc.Injector.INJECTOR;

public class ContractExecutorServer implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(ContractExecutorServer.class);
    private ContractExecutor.Processor processor;

    @Inject
    ApplicationProperties property;

    public ContractExecutorServer(){
        INJECTOR.component.inject(this);
        processor = new ContractExecutor.Processor(new ContractExecutorHandler());
        new Thread(this).start();
    }

    @Override
    public void run() {
        serverStart(processor);
    }

    private void serverStart(ContractExecutor.Processor processor) {
        try {
            TServerTransport serverTransport = new TServerSocket(property.executorPort);
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            logger.info("Starting the Thrift server on port {}...", property.executorPort);
            server.serve();
        } catch (TTransportException e) {
            logger.error("Cannot start Thrift server on port " + property.executorPort + ". " + e.getMessage(), e);
        }
    }
}

package com.credits.ioc;

import com.credits.ApplicationProperties;
import com.credits.client.node.service.NodeApiService;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.service.node.api.NodeApiInteractionService;
import com.credits.service.node.api.NodeApiInteractionServiceThriftImpl;
import com.credits.service.node.apiexec.NodeApiExecInteractionService;
import com.credits.service.node.apiexec.NodeApiExecInteractionServiceThriftImpl;
import com.credits.service.node.apiexec.NodeApiExecService;
import com.credits.service.node.apiexec.NodeApiExecServiceImpl;
import dagger.Module;
import dagger.Provides;

import javax.inject.Inject;
import javax.inject.Singleton;

@Module
public class AppModule {

    @Inject
    ApplicationProperties properties;

    @Singleton
    @Provides
    public NodeApiInteractionService provideLevelDbInteractionService() {
        return new NodeApiInteractionServiceThriftImpl();
    }


    @Singleton
    @Provides
    public NodeApiService provideNodeThriftApi(ApplicationProperties properties) {
        return NodeApiServiceImpl.getInstance(properties.apiHost, properties.apiPort);
    }

    @Singleton
    @Provides
    public NodeApiExecService provideNodeThriftApiExec(ApplicationProperties properties) {
        return NodeApiExecServiceImpl.getInstance(properties.apiHost, properties.executorNodeApiPort);
    }

    @Singleton
    @Provides
    public ApplicationProperties provideProperties() {
        return new ApplicationProperties();
    }

    @Singleton
    @Provides
    public NodeApiExecInteractionService provideNodeApiExecInteractionService() {
        return new NodeApiExecInteractionServiceThriftImpl();
    }
}

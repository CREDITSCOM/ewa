package com.credits.ioc;

import com.credits.ApplicationProperties;
import com.credits.client.node.service.NodeApiService;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.client.node.util.TransactionIdCalculateUtils;
import com.credits.service.node.api.NodeApiInteractionService;
import com.credits.service.node.api.NodeApiInteractionServiceThriftImpl;
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
    public ApplicationProperties provideProperties() {
        return new ApplicationProperties();
    }

    @Singleton
    @Provides
    public TransactionIdCalculateUtils provideTransactionIdCalculateUtils() {
        return new TransactionIdCalculateUtils();
    }

}

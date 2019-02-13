package com.credits.service;

import com.credits.ioc.AppModule;
import com.credits.service.contract.ContractExecutorService;
import com.credits.service.contract.ContractExecutorServiceImpl;
import com.credits.service.node.api.NodeApiInteractionService;
import dagger.Module;
import dagger.Provides;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;

@Module
public class TestModule extends AppModule {
    public TestModule() {
        super();
    }

    @Inject
    NodeApiInteractionService nodeApiInteractionService;


    @Provides
    public ContractExecutorService provideContractExecutorService(){
        return new ContractExecutorServiceImpl(mock(NodeApiInteractionService.class));
    }

}

package com.credits.service;

import com.credits.ioc.AppModule;
import com.credits.service.contract.ContractExecutorService;
import com.credits.service.contract.ContractExecutorServiceImpl;
import dagger.Module;
import dagger.Provides;

@Module
public class TestModule extends AppModule {
    public TestModule() {
        super();
    }

    @Provides
    public ContractExecutorService provideContractExecutorService(){
        return new ContractExecutorServiceImpl();
    }

}

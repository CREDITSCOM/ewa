package com.credits.ioc;

import com.credits.ApplicationProperties;
import com.credits.thrift.ContractExecutorHandler;
import com.credits.thrift.ContractExecutorServer;
import dagger.Module;
import dagger.Provides;
import service.executor.ContractExecutorService;

import javax.inject.Singleton;

@Module(includes = {CEServiceModule.class, AppModule.class})
public class CEServerModule {

    @Singleton
    @Provides
    public ContractExecutorServer provideContractExecutorServer(ContractExecutorHandler contractExecutorHandler,
                                                                ApplicationProperties applicationProperties){
        return new ContractExecutorServer(contractExecutorHandler, applicationProperties);
    }

    @Singleton
    @Provides
    public ContractExecutorHandler providesContractExecutorHandler(ContractExecutorService contractExecutorService){
        return new ContractExecutorHandler(contractExecutorService);
    }

}

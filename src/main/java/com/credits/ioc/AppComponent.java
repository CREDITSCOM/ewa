package com.credits.ioc;

import com.credits.service.contract.ContractExecutorServiceImpl;
import com.credits.service.db.leveldb.NodeApiInteractionServiceThriftImpl;
import com.credits.thrift.ContractExecutorServer;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(ContractExecutorServiceImpl contractExecutorService);
    void inject(NodeApiInteractionServiceThriftImpl levelDbInteractionServiceThrift);
    void inject(ContractExecutorServer contractExecutorServer);
    void inject(AppModule appModule);
}

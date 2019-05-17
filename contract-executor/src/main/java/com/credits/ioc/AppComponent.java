package com.credits.ioc;

import com.credits.service.contract.ContractExecutorServiceImpl;
import com.credits.service.node.apiexec.NodeApiExecInteractionServiceImpl;
import com.credits.thrift.ContractExecutorHandler;
import com.credits.thrift.ContractExecutorServer;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(ContractExecutorServiceImpl contractExecutorService);
    void inject(NodeApiExecInteractionServiceImpl nodeApiExecInteractionServiceImpl);
    void inject(ContractExecutorServer contractExecutorServer);
    void inject(ContractExecutorHandler contractExecutorHandler);
}

package com.credits.ioc;

import com.credits.secure.PermissionsManager;
import com.credits.service.contract.ContractExecutorServiceImpl;
import com.credits.service.node.api.NodeApiInteractionServiceThriftImpl;
import com.credits.service.node.apiexec.NodeApiExecInteractionServiceThriftImpl;
import com.credits.thrift.ContractExecutorHandler;
import com.credits.thrift.ContractExecutorServer;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(PermissionsManager permissionManager);
    void inject(ContractExecutorServiceImpl contractExecutorService);
    void inject(NodeApiInteractionServiceThriftImpl levelDbInteractionServiceThrift);
    void inject(NodeApiExecInteractionServiceThriftImpl nodeApiExecInteractionServiceThriftImpl);
    void inject(ContractExecutorServer contractExecutorServer);
    void inject(AppModule appModule);
    void inject(ContractExecutorHandler contractExecutorHandler);
}

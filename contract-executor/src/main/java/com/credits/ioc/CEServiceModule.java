package com.credits.ioc;

import com.credits.ApplicationProperties;
import com.credits.secure.PermissionsManager;
import com.credits.service.contract.ContractExecutorServiceImpl;
import com.credits.service.node.apiexec.NodeApiExecInteractionServiceImpl;
import com.credits.service.node.apiexec.NodeThriftApiExec;
import com.credits.service.node.apiexec.NodeThriftApiExecClient;
import dagger.Module;
import dagger.Provides;
import service.executor.ContractExecutorService;
import service.node.NodeApiExecInteractionService;

import javax.inject.Singleton;

@Module(includes = AppModule.class)
public class CEServiceModule {

    @Singleton
    @Provides
    public ContractExecutorService provideContractExecutorService(NodeApiExecInteractionService nodeApiExecService, PermissionsManager permissionManager){
        return new ContractExecutorServiceImpl(nodeApiExecService, permissionManager);
    }

    @Singleton
    @Provides
    public NodeApiExecInteractionService provideNodeApiExecInteractionService(NodeThriftApiExec nodeThriftApiClient) {
        return new NodeApiExecInteractionServiceImpl(nodeThriftApiClient);
    }

    @Singleton
    @Provides
    public NodeThriftApiExec provideNodeThriftApi(ApplicationProperties properties){
        return new NodeThriftApiExecClient(properties.apiHost, properties.apiPort);
    }
}

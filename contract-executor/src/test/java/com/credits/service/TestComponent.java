package com.credits.service;

import com.credits.thrift.ContractExecutorHandlerTest;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Created by Igor Goryunov on 10.10.2018
 */
@Singleton
@Component(modules = {TestModule.class})
public interface TestComponent{
    void inject(ServiceTest serviceTest);
    void inject(ContractExecutorHandlerTest contractExecutorHandlerTest);
    void inject(TestModule module);
}

package com.credits.service;

import com.credits.ioc.AppComponent;
import com.credits.service.contract.ThriftIntegrationMockTest;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Created by Igor Goryunov on 10.10.2018
 */
@Singleton
@Component(modules = {TestModule.class})
public interface TestComponent extends AppComponent {
    void inject(ServiceTest serviceTest);
    void inject(ThriftIntegrationMockTest thriftIntegrationMockTest);
}

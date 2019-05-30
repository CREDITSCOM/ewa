package tests.credits;

import dagger.Component;
import tests.credits.service.ServiceTest;
import tests.credits.thrift.ContractExecutorHandlerTest;

import javax.inject.Singleton;


@Singleton
@Component(modules = {TestModule.class})
public interface TestComponent{
    void inject(ServiceTest serviceTest);
    void inject(ContractExecutorHandlerTest contractExecutorHandlerTest);
}

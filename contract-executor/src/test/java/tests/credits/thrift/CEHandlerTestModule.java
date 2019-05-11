package tests.credits.thrift;

import com.credits.thrift.ContractExecutorHandler;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import service.executor.ContractExecutorService;

import javax.inject.Singleton;

import static org.mockito.Mockito.mock;

@Module
public class CEHandlerTestModule{

   @Singleton
   @Provides
   public ContractExecutorService provideMockContractExecutorService(){
      return mock(ContractExecutorService.class);
   }

   @Singleton
   @Provides
   public ContractExecutorHandler provideContractExecutorHandler(ContractExecutorService contractExecutorService){
      return new ContractExecutorHandler(contractExecutorService);
   }
}


@Singleton
@Component(modules = CEHandlerTestModule.class)
interface CEHandlerTestComponent {
   void inject(ContractExecutorHandlerTest contractExecutorHandlerTest);
}

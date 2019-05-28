package tests.credits;

import com.credits.secure.PermissionsManager;
import com.credits.secure.Sandbox;
import com.credits.service.contract.ContractExecutorServiceImpl;
import dagger.Module;
import dagger.Provides;
import service.executor.ContractExecutorService;
import service.node.NodeApiExecInteractionService;

import javax.inject.Singleton;
import java.io.FilePermission;
import java.security.Permission;
import java.security.Permissions;
import java.util.Enumeration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Module
public class TestModule {

    @Provides
    @Singleton
    public ContractExecutorService provideContractExecutorService(NodeApiExecInteractionService nodeApi, PermissionsManager permissionsManager) {
        return new ContractExecutorServiceImpl(nodeApi, permissionsManager);
    }

    @Provides
    @Singleton
    public NodeApiExecInteractionService provideMockNodeApiInteractionService() {
        return mock(NodeApiExecInteractionService.class);
    }

    @Singleton
    @Provides
    public PermissionsManager providesPermissionsManager() {
        PermissionsManager permissionsManager = spy(PermissionsManager.class);
        doAnswer(invocation -> {
            final Class<?> contractClass = invocation.getArgument(0);
            final Permissions permissions = new Permissions();
            final Enumeration<Permission> permissionEnumeration = permissionsManager.getSmartContractPermissions().elements();
            while (permissionEnumeration.hasMoreElements()) {
                permissions.add(permissionEnumeration.nextElement());
            }
            permissions.add(new FilePermission("<<ALL FILES>>", "read"));
            Sandbox.confine(contractClass, permissions);
            return invocation;
        }).when(permissionsManager).dropSmartContractRights(any());
        return permissionsManager;
    }

}

package tests.credits.service;

import com.credits.secure.PermissionsManager;
import com.credits.secure.Sandbox;
import com.credits.service.contract.ContractExecutorServiceImpl;
import dagger.Module;
import dagger.Provides;
import service.executor.ContractExecutorService;
import service.node.NodeApiExecInteractionService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FilePermission;
import java.security.Permission;
import java.security.Permissions;
import java.util.Enumeration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@Module
public class TestModule {

    @Inject
    PermissionsManager permissionsManager;

    @Provides
    public ContractExecutorService provideContractExecutorService(PermissionsManager permissionsManager) {
        return new ContractExecutorServiceImpl(mock(NodeApiExecInteractionService.class), permissionsManager);
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

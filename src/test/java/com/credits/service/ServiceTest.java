package com.credits.service;

import com.credits.App;
import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.service.contract.ContractExecutorServiceImpl;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.FilePermission;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Permissions;

import static com.credits.TestUtils.SimpleInMemoryCompilator.compile;
import static com.credits.TestUtils.encrypt;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {App.class})
@PrepareForTest(ContractExecutorServiceImpl.class)
public abstract class ServiceTest {

    protected final String address = "1a2b3c";

    @Rule
    public PowerMockRule powerMockRunner = new PowerMockRule();

    @Value("${api.server.host}")
    protected String apiServerHost;

    @Value("${api.server.port}")
    protected Integer apiServerPort;

    @Resource
    protected ContractExecutorServiceImpl ceService;

    protected ApiClient mockClient;


    protected void setUp() throws Exception {
        ceService = spy(ceService);
        mockClient = mock(ApiClient.class);
        System.out.println();
        Whitebox.setInternalState(ceService, "ldbClient", mockClient);

        doReturn(testingPermissions()).when(ceService, "createPermissions");
    }

    private Permissions testingPermissions() throws Exception {
        Permissions permissions = Whitebox.invokeMethod(ceService, "createPermissions");
        permissions.add(new FilePermission("\\-", "read"));
        permissions.add(new FilePermission("\\" + System.getProperty("user.home") + "\\.m2\\-", "read")); //permission for using maven repository
        return permissions;
    }

    protected String readSourceCode(String resourcePath) throws IOException {
        String sourceCodePath =
            String.format("%s/src/test/resources/com/credits/service/usercode/%s", Paths.get("").toAbsolutePath(),
                resourcePath);
        return new String(Files.readAllBytes(Paths.get(sourceCodePath)));
    }

    protected byte[] compileSourceCode(String sourceCodePath) throws Exception {
        String sourceCode = readSourceCode(sourceCodePath);
        byte[] bytecode = compile(sourceCode, "Contract", "TKN");
        when(mockClient.getSmartContract(address)).thenReturn(
            new SmartContractData(sourceCode, bytecode, encrypt(bytecode)));
        return bytecode;
    }
}

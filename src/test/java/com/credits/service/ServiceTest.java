package com.credits.service;

import com.credits.App;
import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.service.contract.ContractExecutorServiceImpl;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.credits.TestUtils.SimpleInMemoryCompilator.compile;
import static com.credits.TestUtils.encrypt;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {App.class})
public abstract class ServiceTest {

    @Value("${api.server.host}")
    protected String apiServerHost;

    @Value("${api.server.port}")
    protected Integer apiServerPort;

    @Resource
    protected ContractExecutorServiceImpl ceService;

    protected final String address = "1a2b3c";

    protected ApiClient mockClient;

    protected void setUp() throws Exception {
        mockClient = mock(ApiClient.class);
        Whitebox.setInternalState(ceService, "ldbClient", mockClient);
    }

    protected String readSourceCode(String resourcePath) throws IOException {
        String sourceCodePath =
            String.format("%s/src/test/resources/com/credits/service/usercode/%s", Paths.get("").toAbsolutePath(),
                resourcePath);
        return new String(Files.readAllBytes(Paths.get(sourceCodePath)));
    }

    protected byte[] compileSourceCode(String sourceCodePath) throws Exception {
        String sourceCode = readSourceCode(sourceCodePath);
        byte[]  bytecode = compile(sourceCode,"Contract", "TKN");
        when(mockClient.getSmartContract(address)).thenReturn(
            new SmartContractData(sourceCode, bytecode, encrypt(bytecode)));
        return bytecode;
    }
}

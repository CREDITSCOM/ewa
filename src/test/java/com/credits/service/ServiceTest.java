package com.credits.service;

import com.credits.App;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.leveldb.client.service.LevelDbService;
import com.credits.service.contract.ContractExecutorServiceImpl;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;
import static java.io.File.separator;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {App.class})
public abstract class ServiceTest {

    protected final byte[] address = "1a2b3c".getBytes();

    @Value("${api.server.host}")
    protected String apiServerHost;

    @Value("${api.server.port}")
    protected Integer apiServerPort;

    @Resource
    protected ContractExecutorServiceImpl ceService;

    protected LevelDbService mockLevelDbService;

    protected void setUp() throws Exception {
         mockLevelDbService = mock(LevelDbService.class);
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
        when(mockLevelDbService.getSmartContract(address)).thenReturn(
            new SmartContractData(address, address,sourceCode, bytecode, null,null));
        return bytecode;
    }

    @After
    public void tearDown() {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileSystemUtils.deleteRecursively(new File(dir));
    }
}

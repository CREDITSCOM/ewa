package com.credits.service;

import com.credits.client.node.service.NodeApiService;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.pojo.SmartContractDeployData;
import com.credits.general.thrift.generated.TokenStandart;
import com.credits.general.util.Converter;
import com.credits.service.contract.ContractExecutorService;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;
import static java.io.File.separator;
import static org.mockito.Mockito.when;

public abstract class ServiceTest {

    protected final byte[] address = "1a2b3c".getBytes();
    protected TestComponent testComponent;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Inject
    protected ContractExecutorService ceService;
    @Mock
    protected NodeApiService mockNodeApiService;

    @Before
    public void setUp() throws Exception {
        testComponent = DaggerTestComponent.builder().testModule(new TestModule()).build();
        testComponent.inject(this);
    }

    protected byte[] compileSourceCode(String sourceCodePath) throws Exception {
        String sourceCode = readSourceCode(sourceCodePath);
        byte[] bytecode = compile(sourceCode, "Contract", "TKN");
        when(mockNodeApiService.getSmartContract(Converter.encodeToBASE58(address))).thenReturn(new SmartContractData(
                address,
                address,
                new SmartContractDeployData(sourceCode, bytecode, TokenStandart.CreditsBasic),
                null
                )
        );
        return bytecode;
    }

    protected String readSourceCode(String resourcePath) throws IOException {
        String sourceCodePath = String.format("%s/src/test/resources/com/credits/service/usercode/%s", Paths.get("").toAbsolutePath(), resourcePath);
        return new String(Files.readAllBytes(Paths.get(sourceCodePath)));
    }

    @After
    public void tearDown() throws IOException {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileUtils.deleteDirectory(new File(dir));
    }
}

package com.credits.service;

import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.pojo.SmartContractDeployData;
import com.credits.client.node.pojo.TokenStandartData;
import com.credits.client.node.service.NodeApiService;
import com.credits.exception.ContractExecutorException;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.service.contract.ContractExecutorService;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.io.File.separator;
import static org.mockito.Mockito.when;

public abstract class ServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(ServiceTest.class);

    protected final byte[] initiatorAddress = "1a2b3c".getBytes();
    protected final byte[] contractAddress = "4d5e6f".getBytes();
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

    public static List<ByteCodeObjectData> compileSourceCode(String sourceCode) {
        CompilationPackage compilationPackage = new InMemoryCompiler().compile(sourceCode);
        if (compilationPackage.isCompilationStatusSuccess()) {
            return GeneralConverter.compilationPackageToByteCodeObjects(compilationPackage);
        } else {
            List<Diagnostic<? extends JavaFileObject>> diagnostics = compilationPackage.getCollector().getDiagnostics();
            diagnostics.forEach(action -> {
                logger.info(String.format("\nLine number: %s; Error message: %s", action.getLineNumber(), action.getMessage(null)));
            });
            throw new ContractExecutorException("Cannot compile sourceCode");
        }
    }


    protected List<ByteCodeObjectData> compileSourceCodeFromFile(String sourceCodePath) throws Exception {
        String sourceCode = readSourceCode(sourceCodePath);
        List<ByteCodeObjectData> byteCodeObjects = compileSourceCode(sourceCode);
        when(mockNodeApiService.getSmartContract(GeneralConverter.encodeToBASE58(initiatorAddress))).thenReturn(new SmartContractData(
                initiatorAddress,
                initiatorAddress,
                new SmartContractDeployData(sourceCode, byteCodeObjects, TokenStandartData.CreditsBasic),
                null
                )
        );
        return byteCodeObjects;
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

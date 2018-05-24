package com.credits.service.contract;

import com.credits.exception.CompilationException;
import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import com.credits.service.usercode.UserCodeStorageService;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.credits.classload.ByteArrayClassLoaderTest.SimpleInMemoryCompilator.compile;

public class ContractExecutorServiceThriftIntegrationTest extends ServiceTest {
    @Resource
    private ContractExecutorService ceService;

    @Resource
    private UserCodeStorageService userCodeService;

    private final String address = "1a2b3c";

    @Before
    public void setUp() throws ContractExecutorException {
        clean(address);

    }

    @Test
    public void executionTest() throws ContractExecutorException {
        String fileName = "ContractExecutorServiceThriftIntegrationTestCode.java";
        File testFile = new File(fileName);
        try (InputStream stream = getClass().getClassLoader()
            .getResourceAsStream("com/credits/service/usercode/" + fileName)) {
            FileUtils.copyToFile(stream, testFile);
            userCodeService.store(testFile, address);
            testFile.delete();
        } catch (ContractExecutorException | IOException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }

        ceService.execute(address, "");
        ceService.execute(address, "foo", null);
    }

    @Test
    public void executeByteCodeTest() throws CompilationException, ContractExecutorException {
        String sourceCode = "public class Contract {\n" + "\n" + "    public Contract() {\n" +
            "        System.out.println(\"Hello World!!\"); \n" +
            "    }\npublic void foo(){\nSystem.out.println(\"Method foo executed\");\n}\n}";
        byte[] bytecode = compile(sourceCode, "Contract", "TKN");
        ceService.execute(address, bytecode, "foo", new String[0]);
    }

}

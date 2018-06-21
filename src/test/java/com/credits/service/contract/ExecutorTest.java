package com.credits.service.contract;

import com.credits.leveldb.client.data.SmartContractData;
import com.credits.service.ServiceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;
import static com.credits.TestUtils.encrypt;
import static java.io.File.separator;
import static junit.framework.TestCase.fail;
import static org.powermock.api.mockito.PowerMockito.when;

public class ExecutorTest extends ServiceTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileSystemUtils.deleteRecursively(new File(dir));
    }

    @Test
    public void execute_bytecode() throws Exception {
        String sourceCode =
            "public class Contract implements java.io.Serializable {\n" + "\n" + "    public Contract() {\n" +
                "        System.out.println(\"Hello World!!\"); \n" +
                "    }\npublic void foo(){\nSystem.out.println(\"Method foo executed\");\n}\n}";
        byte[] bytecode = compile(sourceCode, "Contract", "TKN");

        when(mockClient.getSmartContract(address)).thenReturn(
            new SmartContractData(address, sourceCode, bytecode, null, encrypt(bytecode), null, null));

        ceService.execute(address, bytecode, null, "foo", new String[0]);

        when(mockClient.getSmartContract(address)).thenReturn(
            new SmartContractData(address, sourceCode, bytecode, null, "bad hash", null, null));

        try {
            ceService.execute(address, bytecode, null, "foo", new String[0]);
        } catch (Exception e) {
            System.out.println("bad hash error - " + e.getMessage());
            return;
        }
        fail("incorrect hash validation");
    }

    @Test
    public void save_state_smart_contract() throws Exception {
        String sourceCode = readSourceCode("/serviceTest/Contract.java");
        byte[] bytecode = compile(sourceCode, "Contract", "TKN");
        when(mockClient.getSmartContract(address)).thenReturn(
            new SmartContractData(address, sourceCode, bytecode, null, encrypt(bytecode), null, null));

        byte[] contractState = ceService.execute(address, bytecode, null, "initialize", new String[] {});
        contractState = ceService.execute(address, bytecode, contractState, "printTotal", new String[] {});

        contractState = ceService.execute(address, bytecode, contractState, "addTokens", new String[] {"10"});
        contractState = ceService.execute(address, bytecode, contractState, "printTotal", new String[] {});

        contractState = ceService.execute(address, bytecode, contractState, "addTokens", new String[] {"-11"});
        contractState = ceService.execute(address, bytecode, contractState, "printTotal", new String[] {});
    }
}

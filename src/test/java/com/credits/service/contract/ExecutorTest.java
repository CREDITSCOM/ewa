package com.credits.service.contract;

import com.credits.leveldb.client.data.SmartContractData;
import com.credits.service.ServiceTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.credits.TestUtils.SimpleInMemoryCompilator.compile;
import static com.credits.TestUtils.encrypt;
import static junit.framework.TestCase.fail;
import static org.powermock.api.mockito.PowerMockito.when;

public class ExecutorTest extends ServiceTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void execute_bytecode() throws Exception {
        String sourceCode = "public class Contract {\n" + "\n" + "    public Contract() {\n" +
            "        System.out.println(\"Hello World!!\"); \n" +
            "    }\npublic void foo(){\nSystem.out.println(\"Method foo executed\");\n}\n}";
        byte[] bytecode = compile(sourceCode, "Contract", "TKN");

        when(mockClient.getSmartContract(address)).thenReturn(
            new SmartContractData(address, sourceCode, bytecode, encrypt(bytecode)));

        ceService.execute(address, bytecode, "foo", new String[0]);

        when(mockClient.getSmartContract(address)).thenReturn(new SmartContractData(address, sourceCode, bytecode, "bad hash"));

        try {
            ceService.execute(address, bytecode, "foo", new String[0]);
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
            new SmartContractData(address, sourceCode, bytecode, encrypt(bytecode)));

        ceService.execute(address, bytecode, "initialize", new String[]{});
        ceService.execute(address, bytecode, "printTotal", new String[] {});

        ceService.execute(address, bytecode, "addTokens", new String[] {"10"});
        ceService.execute(address, bytecode, "printTotal", new String[] {});

        ceService.execute(address, bytecode, "addTokens", new String[] {"-11"});
        ceService.execute(address, bytecode, "printTotal", new String[] {});
    }
}

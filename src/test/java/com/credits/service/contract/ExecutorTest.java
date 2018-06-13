package com.credits.service.contract;

import com.credits.leveldb.client.data.SmartContractData;
import com.credits.service.ServiceTest;
import org.junit.Before;
import org.junit.Test;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;
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

        when(mockClient.getSmartContract(address)).thenReturn(new SmartContractData(sourceCode, bytecode, encrypt(bytecode)));

        ceService.execute(address, bytecode, "foo", new String[0]);

        when(mockClient.getSmartContract(address)).thenReturn(new SmartContractData(sourceCode, bytecode, "bad hash"));

        try {
            ceService.execute(address, bytecode, "foo", new String[0]);
        } catch (Exception e) {
            System.out.println("bad hash error - " + e.getMessage());
            return;
        }
        fail("incorrect hash validation");
    }
}

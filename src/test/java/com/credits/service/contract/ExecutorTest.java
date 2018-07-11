package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.service.ServiceTest;
import com.credits.thrift.ReturnValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;
import static com.credits.TestUtils.encrypt;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.when;

public class ExecutorTest extends ServiceTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Ignore //Test ignore because hash validation disabled
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
        } catch (ContractExecutorException e) {
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

        byte[] contractState = ceService.execute(address, bytecode, null, null, null).getContractState();
        contractState = ceService.execute(address, bytecode, contractState, "initialize", new String[] {}).getContractState();
        ReturnValue rvTotalInitialized = ceService.execute(address, bytecode, contractState, "getTotal", new String[] {});
        Assert.assertEquals(1, rvTotalInitialized.getVariant().getFieldValue());

        contractState = ceService.execute(address, bytecode, contractState, "addTokens", new String[] {"10"}).getContractState();
        ReturnValue rvTotalAfterSumming = ceService.execute(address, bytecode, contractState, "getTotal", new String[] {});
        Assert.assertEquals(11, rvTotalAfterSumming.getVariant().getFieldValue());

        contractState = ceService.execute(address, bytecode, contractState, "addTokens", new String[] {"-11"}).getContractState();
        ReturnValue rvTotalAfterSubtraction = ceService.execute(address, bytecode, contractState, "getTotal", new String[] {});
        Assert.assertEquals(0, rvTotalAfterSubtraction.getVariant().getFieldValue());
    }
}

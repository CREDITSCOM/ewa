package com.credits.service.contract;

import com.credits.common.utils.Base58;
import com.credits.exception.ContractExecutorException;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.service.ServiceTest;
import com.credits.thrift.ReturnValue;
import com.credits.thrift.generated.Variant;
import com.credits.thrift.utils.ContractUtils;
import com.credits.thrift.utils.VariantMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.when;

public class ContractExecutorTest extends ServiceTest {

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

        when(mockLevelDbService.getSmartContract(address)).thenReturn(
                new SmartContractData(address, address, sourceCode, bytecode, null));

        ceService.execute(address, bytecode, null, "foo", new Variant[0]);

        when(mockLevelDbService.getSmartContract(address)).thenReturn(
                new SmartContractData(address, address, sourceCode, bytecode, "bad hash"));

        try {
            ceService.execute(address, bytecode, null, "foo", new Variant[0]);
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

        byte[] contractState = ceService.execute(address, bytecode, null, null, null).getContractState();

        contractState = ceService.execute(address, bytecode, contractState, "initialize", new Variant[]{}).getContractState();
        ReturnValue rvTotalInitialized = ceService.execute(address, bytecode, contractState, "getTotal", new Variant[]{});
        Assert.assertEquals(1, rvTotalInitialized.getVariant().getFieldValue());

        contractState = ceService.execute(address, bytecode, contractState, "addTokens", new Variant[]{
                ContractUtils.mapObjectToVariant("10")
        }).getContractState();
        ReturnValue rvTotalAfterSumming = ceService.execute(address, bytecode, contractState, "getTotal", new Variant[]{});
        Assert.assertEquals(11, rvTotalAfterSumming.getVariant().getFieldValue());

        contractState = ceService.execute(address, bytecode, contractState, "addTokens", new Variant[]{
                ContractUtils.mapObjectToVariant("10")
        }).getContractState();
        ReturnValue rvTotalAfterSubtraction = ceService.execute(address, bytecode, contractState, "getTotal", new Variant[]{});
        Assert.assertEquals(0, rvTotalAfterSubtraction.getVariant().getFieldValue());
    }

    @Test
    public void initiator_init() throws Exception {
        String sourceCode = readSourceCode("/serviceTest/Contract.java");
        byte[] bytecode = compile(sourceCode, "Contract", "TKN");

        byte[] contractState = ceService.execute(address, bytecode, null, null, null).getContractState();

        ReturnValue result = ceService.execute(address, bytecode, contractState, "getInitiatorAddress", null);
        assertEquals(Base58.encode(address), result.getVariant().getV_string());
    }
}
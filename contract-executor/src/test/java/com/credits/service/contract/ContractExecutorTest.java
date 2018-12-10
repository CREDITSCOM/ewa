package com.credits.service.contract;

import com.credits.client.executor.pojo.MethodDescriptionData;
import com.credits.exception.ContractExecutorException;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.pojo.SmartContractDeployData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Base58;
import com.credits.general.util.GeneralConverter;
import com.credits.service.ServiceTest;
import com.credits.thrift.ReturnValue;
import com.credits.thrift.utils.ContractUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
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

        when(mockNodeApiService.getSmartContract(GeneralConverter.encodeToBASE58(address))).thenReturn(
                new SmartContractData(
                        address,
                        address,
                        new SmartContractDeployData(sourceCode, bytecode, (short)0),
                        null
                ));

        ceService.execute(address, bytecode, null, "foo", new Variant[0]);

        when(mockNodeApiService.getSmartContract(GeneralConverter.encodeToBASE58(address))).thenReturn(
                new SmartContractData(
                        address,
                        address,
                        new SmartContractDeployData(sourceCode, bytecode, (short)0),
                        null
                ));

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
        assertEquals(1, rvTotalInitialized.getVariant().getFieldValue());

        contractState = ceService.execute(address, bytecode, contractState, "addTokens", new Variant[]{
                ContractUtils.mapObjectToVariant(10)
        }).getContractState();
        ReturnValue rvTotalAfterSumming = ceService.execute(address, bytecode, contractState, "getTotal", new Variant[]{});
        assertEquals(11, rvTotalAfterSumming.getVariant().getFieldValue());

        contractState = ceService.execute(address, bytecode, contractState, "addTokens", new Variant[]{
                ContractUtils.mapObjectToVariant(-11)
        }).getContractState();
        ReturnValue rvTotalAfterSubtraction = ceService.execute(address, bytecode, contractState, "getTotal", new Variant[]{});
        assertEquals(0, rvTotalAfterSubtraction.getVariant().getFieldValue());
    }

    @Test
    public void initiator_init() throws Exception {
        String sourceCode = readSourceCode("/serviceTest/Contract.java");
        byte[] bytecode = compile(sourceCode, "Contract", "TKN");

        byte[] contractState = ceService.execute(address, bytecode, null, null, null).getContractState();

        ReturnValue result = ceService.execute(address, bytecode, contractState, "getInitiatorAddress", null);
        assertEquals(Base58.encode(address), result.getVariant().getV_string());
    }

    @Test
    public void get_methods_of_contract() throws Exception{
        String sourceCode = readSourceCode("/serviceTest/Contract.java");
        byte[] bytecode = compile(sourceCode, "Contract", "TKN");

        List<MethodDescriptionData> expectedMethods = Arrays.asList(
            new MethodDescriptionData("initialize", new ArrayList<>(), "void"),
            new MethodDescriptionData("addTokens", singletonList("int"), "void"),
            new MethodDescriptionData("printTotal", new ArrayList<>(), "void"),
            new MethodDescriptionData("getTotal", new ArrayList<>(), "int"),
            new MethodDescriptionData("getInitiatorAddress",new ArrayList<>(), "java.lang.String"));

        assertTrue(ceService.getContractsMethods(bytecode).containsAll(expectedMethods));
    }
}
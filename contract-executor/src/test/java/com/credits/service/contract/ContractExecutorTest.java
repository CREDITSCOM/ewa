package com.credits.service.contract;


import com.credits.client.executor.pojo.MethodDescriptionData;
import com.credits.exception.ContractExecutorException;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.pojo.SmartContractDeployData;
import com.credits.general.thrift.generated.MethodArgument;
import com.credits.general.thrift.generated.TokenStandart;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Base58;
import com.credits.general.util.Converter;
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

        when(mockNodeApiService.getSmartContract(Converter.encodeToBASE58(address))).thenReturn(
                new SmartContractData(
                        address,
                        address,
                        new SmartContractDeployData(sourceCode, bytecode, TokenStandart.CreditsBasic),
                        null
                ));

        ceService.execute(address, bytecode, null, "foo", new Variant[][]{},500);

        when(mockNodeApiService.getSmartContract(Converter.encodeToBASE58(address))).thenReturn(
                new SmartContractData(
                        address,
                        address,
                        new SmartContractDeployData(sourceCode, bytecode, TokenStandart.CreditsBasic),
                        null
                ));

        try {
            ceService.execute(address, bytecode, null, "foo", new Variant[][]{}, 500L);
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

        byte[] contractState = ceService.execute(address, bytecode, null, null, null, 500L).getContractState();

        contractState = ceService.execute(address, bytecode, contractState, "initialize", new Variant[][]{},500L).getContractState();
        ReturnValue rvTotalInitialized = ceService.execute(address, bytecode, contractState, "getTotal", new Variant[][]{},500L);
        assertEquals(1, rvTotalInitialized.getVariantsList().get(0).getFieldValue());

        contractState = ceService.execute(address, bytecode, contractState, "addTokens", new Variant[][]{
            {ContractUtils.mapObjectToVariant(10)}
        },500L).getContractState();
        ReturnValue rvTotalAfterSumming = ceService.execute(address, bytecode, contractState, "getTotal", new Variant[][]{},500L);
        assertEquals(11, rvTotalInitialized.getVariantsList().get(0).getFieldValue());

        contractState = ceService.execute(address, bytecode, contractState, "addTokens", new Variant[][]{
            {ContractUtils.mapObjectToVariant(-11)}
        },500L).getContractState();
        ReturnValue rvTotalAfterSubtraction = ceService.execute(address, bytecode, contractState, "getTotal", new Variant[][]{},500L);
        assertEquals(0, rvTotalInitialized.getVariantsList().get(0).getFieldValue());
    }

    @Test
    public void initiator_init() throws Exception {
        String sourceCode = readSourceCode("/serviceTest/Contract.java");
        byte[] bytecode = compile(sourceCode, "Contract", "TKN");

        byte[] contractState = ceService.execute(address, bytecode, null, null, null, 500L).getContractState();

        ReturnValue result = ceService.execute(address, bytecode, contractState, "getInitiatorAddress", null, 500L);
        assertEquals(Base58.encode(address), result.getVariantsList().get(0).getV_string());
    }

    @Test
    public void get_methods_of_contract() throws Exception{
        String sourceCode = readSourceCode("/serviceTest/Contract.java");
        byte[] bytecode = compile(sourceCode, "Contract", "TKN");

        List<MethodDescriptionData> expectedMethods = Arrays.asList(
            new MethodDescriptionData("void","initialize", new ArrayList<>()),
            new MethodDescriptionData("void", "addTokens", singletonList(new MethodArgument("int", "amount"))),
            new MethodDescriptionData("void", "printTotal", new ArrayList<>()),
            new MethodDescriptionData("int", "getTotal", new ArrayList<>()),
            new MethodDescriptionData("java.lang.String","getInitiatorAddress", new ArrayList<>()));

        assertTrue(ceService.getContractsMethods(bytecode).containsAll(expectedMethods));
    }
}
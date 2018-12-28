package com.credits.service.contract;


import com.credits.client.executor.pojo.MethodDescriptionData;
import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.pojo.SmartContractDeployData;
import com.credits.client.node.thrift.generated.TokenStandart;
import com.credits.exception.CompilationException;
import com.credits.exception.ContractExecutorException;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.VariantData;
import com.credits.general.pojo.VariantType;
import com.credits.general.thrift.generated.MethodArgument;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Base58;
import com.credits.general.util.GeneralConverter;
import com.credits.service.ServiceTest;
import com.credits.thrift.ReturnValue;
import com.credits.thrift.utils.ContractExecutorUtils;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
                "public class Contract implements java.io.Serializable {\n" + "\n" + "    public Contract(String initiator) {\n" +
                        "        System.out.println(\"Hello World!!\"); \n" +
                        "    }\npublic void foo(){\nSystem.out.println(\"Method foo executed\");\n}\n}";
        List<ByteCodeObjectData> byteCodeObjectDataList =
            compileSourceCode(sourceCode);

        when(mockNodeApiService.getSmartContract(GeneralConverter.encodeToBASE58(address))).thenReturn(
                new SmartContractData(
                        address,
                        address,
                        new SmartContractDeployData(sourceCode, byteCodeObjectDataList, TokenStandart.CreditsBasic),
                        null
                ));

        ceService.execute(address, byteCodeObjectDataList, null, "foo", new Variant[][]{{}},500);

        when(mockNodeApiService.getSmartContract(GeneralConverter.encodeToBASE58(address))).thenReturn(
                new SmartContractData(
                        address,
                        address,
                        new SmartContractDeployData(sourceCode, byteCodeObjectDataList, TokenStandart.CreditsBasic),
                        null
                ));

        try {
            ceService.execute(address, byteCodeObjectDataList, null, "foo", new Variant[][]{{}}, 500L);
        } catch (ContractExecutorException e) {
            System.out.println("bad hash error - " + e.getMessage());
            return;
        }
        fail("incorrect hash validation");
    }

    @Test
    public void save_state_smart_contract() throws Exception {
        String sourceCode = readSourceCode("/serviceTest/Contract.java");
        List<ByteCodeObjectData> byteCodeObjectDataList =
            compileSourceCode(sourceCode);

        byte[] contractState = ceService.execute(address, byteCodeObjectDataList, null, null, null, 500L).getContractState();

        contractState = ceService.execute(address, byteCodeObjectDataList, contractState, "initialize", new Variant[][]{{}},500L).getContractState();
        ReturnValue rvTotalInitialized = ceService.execute(address, byteCodeObjectDataList, contractState, "getTotal", new Variant[][]{{}},500L);
        assertEquals(1, rvTotalInitialized.getVariantsList().get(0).getFieldValue());

        contractState = ceService.execute(address, byteCodeObjectDataList, contractState, "addTokens", new Variant[][]{
            {ContractExecutorUtils.mapVariantDataToVariant(new VariantData(VariantType.INT, 10))}
        },500L).getContractState();
        ReturnValue rvTotalAfterSumming = ceService.execute(address, byteCodeObjectDataList, contractState, "getTotal", new Variant[][]{{}},500L);
        assertEquals(11, rvTotalAfterSumming.getVariantsList().get(0).getFieldValue());

        contractState = ceService.execute(address, byteCodeObjectDataList, contractState, "addTokens", new Variant[][]{
            {ContractExecutorUtils.mapVariantDataToVariant(new VariantData(VariantType.INT, -11))}
        },500L).getContractState();
        ReturnValue rvTotalAfterSubtraction = ceService.execute(address, byteCodeObjectDataList, contractState, "getTotal", new Variant[][]{{}},500L);
        assertEquals(0, rvTotalAfterSubtraction.getVariantsList().get(0).getFieldValue());
    }

    @Test
    public void initiator_init() throws Exception {
        String sourceCode = readSourceCode("/serviceTest/Contract.java");
        List<ByteCodeObjectData> byteCodeObjectDataList =
            compileSourceCode(sourceCode);

        byte[] contractState = ceService.execute(address, byteCodeObjectDataList, null, null, null, 500L).getContractState();

        ReturnValue result = ceService.execute(address, byteCodeObjectDataList, contractState, "getInitiatorAddress", new Variant[][]{{}}, 500L);
        assertEquals(Base58.encode(address), result.getVariantsList().get(0).getV_string());
    }

    @Test
    public void get_methods_of_contract() throws Exception{
        String sourceCode = readSourceCode("/serviceTest/Contract.java");
        List<ByteCodeObjectData> byteCodeObjectDataList =
            compileSourceCode(sourceCode);

        List<MethodDescriptionData> expectedMethods = Arrays.asList(
            new MethodDescriptionData("void","initialize", new ArrayList<>()),
            new MethodDescriptionData("void", "addTokens", singletonList(new MethodArgument("int", "amount"))),
            new MethodDescriptionData("void", "printTotal", new ArrayList<>()),
            new MethodDescriptionData("int", "getTotal", new ArrayList<>()),
            new MethodDescriptionData("java.lang.String","getInitiatorAddress", new ArrayList<>()));

        List<MethodDescriptionData> contractsMethods = ceService.getContractsMethods(byteCodeObjectDataList);
        assertTrue(contractsMethods.containsAll(expectedMethods));
    }

    @Test
    public void get_contract_variables() throws Exception{
        String sourceCode = readSourceCode("/serviceTest/Contract.java");
        List<ByteCodeObjectData> byteCodeObjectDataList =
            compileSourceCode(sourceCode);
        byte[] contractState = ceService.execute(address, byteCodeObjectDataList, null, null, null, 500).getContractState();
        Map<String, Variant> contractVariables = ceService.getContractVariables(byteCodeObjectDataList, contractState);
        Assert.assertTrue(contractVariables.containsKey("total"));
    }



    @Test
    public void multipleMethodCall() throws Exception {
        String sourceCode = readSourceCode("/serviceTest/Contract.java");
        List<ByteCodeObjectData> byteCodeObjectDataList =
            compileSourceCode(sourceCode);
        byte[] contractState = ceService.execute(address, byteCodeObjectDataList, null, null, null, 500).getContractState();

        ReturnValue singleCallResult = ceService.execute(address, byteCodeObjectDataList, contractState, "addTokens", new Variant[][] {{Variant.v_int(10)}}, 500);
        ReturnValue multiplyCallResult = ceService.execute(address, byteCodeObjectDataList, contractState, "addTokens", new Variant[][]{
            {Variant.v_int(10)},
            {Variant.v_int(10)},
            {Variant.v_int(10)},
            {Variant.v_int(10)}}, 500);
        assertNotEquals(singleCallResult.getContractState(), multiplyCallResult.getContractState());

        singleCallResult = ceService.execute(address, byteCodeObjectDataList, contractState, "getTotal", new Variant[][]{{}}, 500);
        TestCase.assertEquals(0, singleCallResult.getVariantsList().get(0).getV_int_box());
    }

    @Test
    public void compileClassCall() {
        String sourceCode = null;
        try {
            sourceCode = readSourceCode("/serviceTest/Contract.java");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ceService.compileClass(sourceCode);
        } catch (CompilationErrorException | ContractExecutorException | CompilationException e) {
            e.printStackTrace();
        }
    }
}
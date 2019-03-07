package com.credits.service.contract;


import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.pojo.SmartContractDeployData;
import com.credits.client.node.pojo.TokenStandartData;
import com.credits.exception.CompilationException;
import com.credits.exception.ContractExecutorException;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.MethodArgumentData;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.util.GeneralConverter;
import com.credits.service.ServiceTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
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
                "public class MySmartContract implements java.io.Serializable {\n" + "\n" + "    public MySmartContract(String initiator) {\n" +
                        "        System.out.println(\"Hello World!!\"); \n" +
                        "    }\npublic void foo(){\nSystem.out.println(\"Method foo executed\");\n}\n}";
        List<ByteCodeObjectData> byteCodeObjectDataList =
            compileSourceCode(sourceCode);

        when(mockNodeApiService.getSmartContract(GeneralConverter.encodeToBASE58(initiatorAddress))).thenReturn(
                new SmartContractData(
                        initiatorAddress,
                        initiatorAddress,
                        new SmartContractDeployData(sourceCode, byteCodeObjectDataList, TokenStandartData.CreditsBasic),
                        null
                ));

        //fixme
//        ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, null, "foo", new Variant[][]{{}},500);

        when(mockNodeApiService.getSmartContract(GeneralConverter.encodeToBASE58(initiatorAddress))).thenReturn(
                new SmartContractData(
                        initiatorAddress,
                        initiatorAddress,
                        new SmartContractDeployData(sourceCode, byteCodeObjectDataList, TokenStandartData.CreditsBasic),
                        null
                ));

        try {
            //fixme
//            ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, null, "foo", new Variant[][]{{}}, 500L);
        } catch (ContractExecutorException e) {
            System.out.println("bad hash error - " + e.getMessage());
            return;
        }
        fail("incorrect hash validation");
    }

    @Test
    public void save_state_smart_contract() throws Exception {
        String sourceCode = readSourceCode("/serviceTest/MySmartContract.java");
        List<ByteCodeObjectData> byteCodeObjectDataList =
            compileSourceCode(sourceCode);
        //fixme
//        byte[] contractState = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, null, null, null, 500L).getContractState();

//        contractState = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, contractState, "initialize", new Variant[][]{{}},500L).getContractState();
//        ReturnValue rvTotalInitialized = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, contractState, "getTotal", new Variant[][]{{}},500L);
//        assertEquals(1, rvTotalInitialized.getVariantsList().get(0).getFieldValue());

//        contractState = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, contractState, "addTokens", new Variant[][]{
//            {ContractExecutorUtils.mapVariantDataToVariant(new VariantData(VariantType.INT, 10))}
//        },500L).getContractState();
//        ReturnValue rvTotalAfterSumming = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, contractState, "getTotal", new Variant[][]{{}},500L);
//        assertEquals(11, rvTotalAfterSumming.getVariantsList().get(0).getFieldValue());
//
//        contractState = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, contractState, "addTokens", new Variant[][]{
//            {ContractExecutorUtils.mapVariantDataToVariant(new VariantData(VariantType.INT, -11))}
//        },500L).getContractState();
//        ReturnValue rvTotalAfterSubtraction = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, contractState, "getTotal", new Variant[][]{{}},500L);
//        assertEquals(0, rvTotalAfterSubtraction.getVariantsList().get(0).getFieldValue());
    }

    @Test
    public void initiator_init() throws Exception {
        String sourceCode = readSourceCode("/serviceTest/MySmartContract.java");
        List<ByteCodeObjectData> byteCodeObjectDataList =
            compileSourceCode(sourceCode);
//
//        byte[] contractState = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, null, null, null, 500L).getContractState();
//
//        ReturnValue result = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, contractState, "getInitiatorAddress", new Variant[][]{{}}, 500L);
//        assertEquals(Base58.encode(initiatorAddress), result.getVariantsList().get(0).getV_string());
    }

    @Test
    public void innerSmartsTest() throws Exception {
        String sourceCode = readSourceCode("/serviceTest/MySmartContract.java");
        List<ByteCodeObjectData> byteCodeObjectDataList =
            compileSourceCode(sourceCode);
//
//        byte[] contractState = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, null, null, null, 500L).getContractState();
//
/*
        ReturnValue result = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, contractState, "getInitiatorAddress", new Variant[][]{{}}, 500L);
//*/
//        SmartContractGetResultData smartContractGetResultData =
//            new SmartContractGetResultData(new ApiResponseData(null, "success"), byteCodeObjectDataList, contractState,
//                true);
//        ReturnValue result = ceService.executeExternalSmartContract(0, Base58.encode(initiatorAddress), Base58.encode(contractAddress),
//                "getInitiatorAddress", new ArrayList<>(), smartContractGetResultData, new HashMap<>());
//        assertEquals(Base58.encode(initiatorAddress), result.getVariantsList().get(0).getV_string());
    }


    @Test
    public void get_methods_of_contract() throws Exception{
        String sourceCode = readSourceCode("/serviceTest/MySmartContract.java");
        List<ByteCodeObjectData> byteCodeObjectDataList =
            compileSourceCode(sourceCode);

        List<MethodDescriptionData> expectedMethods = Arrays.asList(
            new MethodDescriptionData("void","initialize", new ArrayList<>(),new ArrayList<>()),
            new MethodDescriptionData("void", "addTokens", Collections.singletonList(new MethodArgumentData("int", "amount", new ArrayList<>())),new ArrayList<>()),
            new MethodDescriptionData("void", "printTotal", new ArrayList<>(),new ArrayList<>()),
            new MethodDescriptionData("int", "getTotal", new ArrayList<>(),new ArrayList<>()),
            new MethodDescriptionData("java.lang.String","getInitiatorAddress", new ArrayList<>(),new ArrayList<>()));

        List<MethodDescriptionData> contractsMethods = ceService.getContractsMethods(byteCodeObjectDataList);
        assertTrue(contractsMethods.containsAll(expectedMethods));
    }

    @Test
    public void get_contract_variables() throws Exception{
        String sourceCode = readSourceCode("/serviceTest/MySmartContract.java");
        List<ByteCodeObjectData> byteCodeObjectDataList =
            compileSourceCode(sourceCode);
//        byte[] contractState = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, null, null, null, 500).getContractState();
//        Map<String, Variant> contractVariables = ceService.getContractVariables(byteCodeObjectDataList, contractState);
//        Assert.assertTrue(contractVariables.containsKey("total"));
    }



    @Test
    public void multipleMethodCall() throws Exception {
        String sourceCode = readSourceCode("/serviceTest/MySmartContract.java");
        List<ByteCodeObjectData> byteCodeObjectDataList =
            compileSourceCode(sourceCode);
//        byte[] contractState = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, null, null, null, 500).getContractState();

//        ReturnValue singleCallResult = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, contractState, "addTokens", new Variant[][] {{Variant.v_int(10)}}, 500);
//        ReturnValue multiplyCallResult = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, contractState, "addTokens", new Variant[][]{
//            {Variant.v_int(10)},
//            {Variant.v_int(10)},
//            {Variant.v_int(10)},
//            {Variant.v_int(10)}}, 500);
//        assertNotEquals(singleCallResult.getContractState(), multiplyCallResult.getContractState());
//
//        singleCallResult = ceService.execute(0, initiatorAddress, contractAddress, byteCodeObjectDataList, contractState, "getTotal", new Variant[][]{{}}, 500);
//        TestCase.assertEquals(0, singleCallResult.getVariantsList().get(0).getV_int_box());
    }

    @Test
    public void compileClassCall() {
        String sourceCode = null;
        try {
            sourceCode = readSourceCode("/serviceTest/MySmartContract.java");
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
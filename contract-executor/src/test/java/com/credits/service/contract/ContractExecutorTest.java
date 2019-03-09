package com.credits.service.contract;


import com.credits.exception.CompilationException;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.pojo.MethodArgumentData;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Base58;
import com.credits.service.ServiceTest;
import com.credits.thrift.ReturnValue;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ContractExecutorTest extends ServiceTest {

    public ContractExecutorTest() {
        super("/serviceTest/MySmartContract.java");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void save_state_smart_contract() {
        byte[] contractState = deploySmartContract().newContractState;

        contractState = executeSmartContract("initialize", contractState).newContractState;

        ReturnValue rvTotalInitialized = executeSmartContract("getTotal", contractState);
        assertEquals(1, rvTotalInitialized.executeResults.get(0).result.getV_int_box()); //fixme must be int

        contractState = executeSmartContract("addTokens", new Variant[][] {{Variant.v_int(10)}}, contractState).newContractState;
        ReturnValue rvTotalAfterSumming = executeSmartContract("getTotal", contractState);
        assertEquals(11, rvTotalAfterSumming.executeResults.get(0).result.getV_int_box());

        contractState = executeSmartContract("addTokens", new Variant[][] {{Variant.v_int(-11)}}, contractState).newContractState;
        ReturnValue rvTotalAfterSubtraction = executeSmartContract("getTotal", contractState);
        assertEquals(0, rvTotalAfterSubtraction.executeResults.get(0).result.getV_int_box());
    }

    @Test
    public void initiator_init() throws Exception {
        byte[] contractState = deploySmartContract().newContractState;

        ReturnValue result = executeSmartContract("getInitiatorAddress", contractState);
        assertEquals(Base58.encode(initiatorAddress), result.executeResults.get(0).result.getV_string());
    }

    @Test
    public void use_inner_structures() {
        //todo add tests
    }


    @Test
    public void get_methods_of_contract() {

        List<MethodDescriptionData> expectedMethods = Arrays.asList(
            new MethodDescriptionData("void", "initialize", new ArrayList<>(), new ArrayList<>()),
            new MethodDescriptionData(
                "void",
                "addTokens",
                singletonList(new MethodArgumentData("int", "amount", new ArrayList<>())),
                new ArrayList<>()),
            new MethodDescriptionData("void", "printTotal", new ArrayList<>(), new ArrayList<>()),
            new MethodDescriptionData("int", "getTotal", new ArrayList<>(), new ArrayList<>()),
            new MethodDescriptionData("java.lang.String", "getInitiatorAddress", new ArrayList<>(), new ArrayList<>()));

        List<MethodDescriptionData> contractsMethods = ceService.getContractsMethods(byteCodeObjectDataList);
        assertTrue(contractsMethods.containsAll(expectedMethods));
    }

    @Test
    public void get_contract_variables() {
        byte[] contractState = deploySmartContract().newContractState;
        Map<String, Variant> contractVariables = ceService.getContractVariables(byteCodeObjectDataList, contractState);
        Assert.assertTrue(contractVariables.containsKey("total"));
    }


    @Test
    public void multipleMethodCall() {
        byte[] contractState = deploySmartContract().newContractState;

        ReturnValue singleCallResult = executeSmartContract("addTokens", new Variant[][] {{Variant.v_int(10)}}, contractState);

        ReturnValue multiplyCallResult = executeSmartContract(
            "addTokens",
            new Variant[][] {
                {Variant.v_int(10)},
                {Variant.v_int(10)},
                {Variant.v_int(10)},
                {Variant.v_int(10)}
            },
            contractState);

        assertNotEquals(singleCallResult.newContractState, multiplyCallResult.newContractState);

        singleCallResult = executeSmartContract("getTotal", contractState);

        TestCase.assertEquals(0, singleCallResult.executeResults.get(0).result.getV_int_box()); //fixme must be int
    }

    @Test
    public void compileClassCall() throws CompilationException, CompilationErrorException {
        ceService.compileClass(sourceCode);
    }

}
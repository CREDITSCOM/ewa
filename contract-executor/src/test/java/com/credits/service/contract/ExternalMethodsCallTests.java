package com.credits.service.contract;

import com.credits.service.ServiceTest;
import com.credits.thrift.ReturnValue;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

public class ExternalMethodsCallTests extends ServiceTest {

    private String calledSmartContractAddress = "5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpe";
    public byte[] deployContractState;

    public ExternalMethodsCallTests() {
        super("/serviceTest/MySmartContract.java");
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        deployContractState = deploySmartContract().newContractState;
    }

    @Test
    public void getter_method_must_not_change_state() {

        configureGetContractByteCodeNodeResponse(deployContractState, false);

        final ReturnValue returnValue = executeExternalSmartContract(
            "externalCall",
            deployContractState,
            calledSmartContractAddress,
            "getTotal");

        final SmartContractMethodResult methodResult = returnValue.executeResults.get(0);

        assertThat(methodResult.status.message, is("success"));
        assertThat(methodResult.result.getV_int_box(), is(0));
        assertThat(returnValue.newContractState, equalTo(deployContractState));
        assertThat(returnValue.newContractState, equalTo(returnValue.externalSmartContracts.get(calledSmartContractAddress).contractState));
    }

    @Test
    public void setter_method_must_return_new_states() {
        configureGetContractByteCodeNodeResponse(deployContractState, true);

        final ReturnValue returnValue = executeExternalSmartContract(
            "recursionExternalContractSetterCall",
            deployContractState,
            10);

        final SmartContractMethodResult methodResult = returnValue.executeResults.get(0);

        assertThat(methodResult.status.message, is("success"));
        assertThat(methodResult.result.getV_int_box(), is(11));
        assertThat(returnValue.newContractState, not(equalTo(deployContractState)));
        assertThat(returnValue.newContractState, not(equalTo(returnValue.externalSmartContracts.get(calledSmartContractAddress).contractState)));
        assertThat(returnValue.externalSmartContracts.size(), is(0));
    }

    @Test
    public void recursion_contract_call(){

        configureGetContractByteCodeNodeResponse(deployContractState, true);

        final ReturnValue returnValue = executeExternalSmartContract(
            "externalCallChangeState",
            deployContractState,
            calledSmartContractAddress,
            "addTokens",
            10);

        final SmartContractMethodResult methodResult = returnValue.executeResults.get(0);

    }

    @Test
    public void call_external_contract_into_constructor() {
        //TODO need implementation
    }
}

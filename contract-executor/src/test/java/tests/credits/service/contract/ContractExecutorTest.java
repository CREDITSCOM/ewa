package tests.credits.service.contract;


import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Base58;
import com.credits.general.util.compiler.CompilationException;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pojo.ReturnValue;
import tests.credits.service.ServiceTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.credits.general.pojo.ApiResponseCode.FAILURE;
import static com.credits.general.thrift.generated.Variant._Fields.V_INT;
import static com.credits.general.thrift.generated.Variant._Fields.V_VOID;
import static com.credits.general.thrift.generated.Variant.v_int;
import static com.credits.general.thrift.generated.Variant.v_string;
import static com.credits.general.util.variant.VariantConverter.VOID_TYPE_VALUE;
import static com.credits.general.util.variant.VariantConverter.toObject;
import static com.credits.utils.ContractExecutorServiceUtils.SUCCESS_API_RESPONSE;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContractExecutorTest extends ServiceTest {

    private byte[] deployContractState;

    public ContractExecutorTest() {
        super("/serviceTest/MySmartContract.java");
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        deployContractState = deploySmartContract().newContractState;
    }

    @Test
    public void returnVoidType() {
        ReturnValue returnValue = executeSmartContract("initialize", deployContractState);
        assertThat(returnValue.executeResults.get(0).result, is(new Variant(V_VOID, VOID_TYPE_VALUE)));
    }

    @Test
    @DisplayName("getter method cannot change contract state")
    public void getterMethodCanNotChangeContractState() {
        ReturnValue rv = executeSmartContract("getTotal", deployContractState);
        assertThat(deployContractState, equalTo(rv.newContractState));
    }

    @Test
    @DisplayName("setter method should be change contract state")
    public void saveStateSmartContract() {
        var executionResult = executeSmartContract("getTotal", deployContractState);
        var total = executionResult.executeResults.get(0).result.getV_int();
        var contractState = executionResult.newContractState;
        assertThat(contractState, equalTo(deployContractState));
        assertThat(total, is(0));

        contractState = executeSmartContract("addTokens", new Variant[][]{{v_int(10)}}, deployContractState).newContractState;
        assertThat(contractState, not(equalTo(deployContractState)));

        executionResult = executeSmartContract("getTotal", contractState);
        total = executionResult.executeResults.get(0).result.getV_int();
        assertThat(total, is(10));
    }

    @Test
    @DisplayName("initiator must be initialized")
    public void initiatorInit() {
        String initiator = executeSmartContract("getInitiatorAddress", deployContractState).executeResults.get(0).result.getV_string();
        assertThat(initiator, is(Base58.encode(initiatorAddress)));
    }

    @Test
    @DisplayName("sendTransaction into smartContract must be call NodeApiExecService")
    public void sendTransactionIntoContract() {
        ReturnValue result = executeSmartContract("payable", new Variant[][]{{v_string("10"), v_string("CS")}}, deployContractState);

        verify(mockNodeApiExecService).sendTransaction(accessId, initiatorAddressBase58, contractAddressBase58, 10, 1.0, new byte[]{});

        assertThat(result.executeResults.get(0).result.getV_boolean(), is(true));
    }

    @Test
    public void getContractVariablesTest() {
        Map<String, Variant> contractVariables = ceService.getContractVariables(byteCodeObjectDataList, deployContractState);
        assertThat(contractVariables, IsMapContaining.hasEntry("total", new Variant(V_INT, 0)));
    }


    @Test
    @DisplayName("returned value can be BigDecimal type")
    public void getBalanceReturnBigDecimal() {
        when(mockNodeApiExecService.getBalance(anyString())).thenReturn(new BigDecimal("19.5"));

        var variantBalance = executeSmartContract("getBalanceTest",
                                                  new Variant[][]{{v_string("qwerty")}},
                                                  deployContractState).executeResults.get(0).result;
        var balance = toObject(variantBalance);

        assertThat(balance, is(new BigDecimal("19.5")));
    }

    @Test
    @DisplayName("multiple call change contract state")
    public void multipleMethodCall() {
        ReturnValue multiplyCallResult = executeSmartContract(
                "addTokens",
                new Variant[][]{
                        {v_int(10)},
                        {v_int(10)},
                        {v_int(10)},
                        {v_int(10)}
                },
                deployContractState);
        int addedTokens = ceService.getContractVariables(byteCodeObjectDataList, multiplyCallResult.newContractState).get("total").getV_int();

        assertThat(multiplyCallResult.newContractState, not(equalTo(deployContractState)));
        assertThat(addedTokens, is(40));
    }


    @Test
    public void compileClassCall() throws CompilationException {
        final List<ByteCodeObjectData> byteCodeObjectData = ceService.compileClass(sourceCode);

        assertThat(byteCodeObjectData, notNullValue());
        assertThat(byteCodeObjectData, not(empty()));
    }


    @Test
    @DisplayName("call NodeApiExecService and returning result")
    public void getSeedCallIntoSmartContract() {
        var seed = new byte[]{0xB, 0xA, 0xB, 0xE};

        when(mockNodeApiExecService.getSeed(anyLong())).thenReturn(seed);
        var executeByteCodeResult = executeSmartContract("testGetSeed", deployContractState).executeResults.get(0).result;

        assertThat(executeByteCodeResult.getV_byte_array(), is(seed));
    }

    @Test
    @DisplayName("execution of smart-contract must be stop when execution time expired")
    public void executionTimeTest() {
        var executionStatus = executeSmartContract("infiniteLoop", deployContractState, 10).executeResults.get(0).status;

        assertThat(executionStatus.code, is(FAILURE.code));
        assertThat(executionStatus.message, containsString("TimeoutException"));
    }

    @Test
    @DisplayName("correct interrupt smart contract if time expired")
    public void correctInterruptContractIfTimeExpired() {
        var executionResult = executeSmartContract("interruptedInfiniteLoop", deployContractState, 10).executeResults.get(0);

        assertThat(executionResult.status, is(SUCCESS_API_RESPONSE));
        assertThat(executionResult.result.getV_string(), is("infinite loop interrupted correctly"));
    }

    @Test
    @DisplayName("wait a bit delay for correct complete smart contract method")
    public void waitCorrectCompleteOfSmartContract() {
        var executionResult = executeSmartContract("interruptInfiniteLoopWithDelay", deployContractState, 10).executeResults.get(0);

        assertThat(executionResult.status, is(SUCCESS_API_RESPONSE));
        assertThat(executionResult.result.getV_string(), is("infinite loop interrupted correctly"));
    }

    @Test
    @DisplayName("executeByteCode must be return spent cpu time by execution method thread")
    public void executeByteCodeMeasureCpuTimeByThread0() {
        var spentCpuTime = executeSmartContract("nothingWorkOnlySleep", deployContractState, 11).executeResults.get(0).spentCpuTime;
        assertThat(spentCpuTime, lessThan(1000_000L));

        spentCpuTime = executeSmartContract("bitWorkingThenSleep", deployContractState, 11).executeResults.get(0).spentCpuTime;
        assertThat(spentCpuTime, greaterThan(10_000_000L));
    }

    @Test
    @DisplayName("exception into executeByteCode must be return fail status with exception message")
    public void exceptionDuringExecution() {
        var result = executeSmartContract("thisMethodThrowsExcetion", deployContractState, 1).executeResults.get(0);

        assertThat(result.status.code, is(FAILURE.code));
        assertThat(result.status.message, containsString("oops some problem"));
    }

    @Test
    @DisplayName("exception into constructor must be return fail status with exception method")
    public void constructorWithException() throws IOException {
        super.selectSourcecode("/serviceTest/TroubleConstructor.java");

        var result = deploySmartContract().executeResults.get(0);

        assertThat(result.status.code, is(FAILURE.code));
        assertThat(result.status.message, containsString("some problem found here"));
    }
}


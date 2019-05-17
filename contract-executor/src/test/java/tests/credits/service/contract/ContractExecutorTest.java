package tests.credits.service.contract;


import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Base58;
import com.credits.general.util.compiler.CompilationException;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import pojo.ReturnValue;
import tests.credits.service.ServiceTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.credits.general.thrift.generated.Variant._Fields.V_BYTE;
import static com.credits.general.thrift.generated.Variant._Fields.V_VOID;
import static com.credits.general.thrift.generated.Variant.v_int;
import static com.credits.general.thrift.generated.Variant.v_string;
import static com.credits.general.util.GeneralConverter.amountToBigDecimal;
import static com.credits.general.util.variant.VariantConverter.VOID_TYPE_VALUE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ContractExecutorTest extends ServiceTest {

    public ContractExecutorTest() {
        super("/serviceTest/MySmartContract.java");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void return_void_type() throws Exception {
        byte[] contractState = deploySmartContract().newContractState;

        ReturnValue returnValue = executeSmartContract("initialize", contractState);
        assertEquals(new Variant(V_VOID, VOID_TYPE_VALUE), returnValue.executeResults.get(0).result);
    }

    @Test
    public void getter_method_have_not_change_contract_state() throws Exception {
        byte[] initContractState = deploySmartContract().newContractState;

        ReturnValue rv = executeSmartContract("getTotal", initContractState);
        assertThat(initContractState, equalTo(rv.newContractState));
    }

    @Test
    public void save_state_smart_contract() throws Exception {
        byte[] contractState = deploySmartContract().newContractState;

        contractState = executeSmartContract("initialize", contractState).newContractState;

        ReturnValue rvTotalInitialized = executeSmartContract("getTotal", contractState);
        assertEquals(1, rvTotalInitialized.executeResults.get(0).result.getV_int());

        contractState = executeSmartContract("addTokens", new Variant[][]{{v_int(10)}}, contractState).newContractState;
        ReturnValue rvTotalAfterSumming = executeSmartContract("getTotal", contractState);
        assertEquals(11, rvTotalAfterSumming.executeResults.get(0).result.getV_int());

        contractState = executeSmartContract("addTokens", new Variant[][]{{v_int(-11)}}, contractState).newContractState;
        ReturnValue rvTotalAfterSubtraction = executeSmartContract("getTotal", contractState);
        assertEquals(0, rvTotalAfterSubtraction.executeResults.get(0).result.getV_int());
    }

    @Test
    public void initiator_init() throws Exception {
        byte[] contractState = deploySmartContract().newContractState;

        ReturnValue result = executeSmartContract("getInitiatorAddress", contractState);
        assertEquals(Base58.encode(initiatorAddress), result.executeResults.get(0).result.getV_string());
    }

    @Test
    public void send_transaction_into_contract() throws Exception {
        byte[] contractState = deploySmartContract().newContractState;

        ReturnValue result = executeSmartContract("payable", new Variant[][]{{v_string("10"), v_string("CS")}}, contractState);
        assertThat(result.executeResults.get(0).result.getV_boolean(), is(true));
    }

    @Test
    public void get_contract_variables() throws Exception {
        byte[] contractState = deploySmartContract().newContractState;
        Map<String, Variant> contractVariables = ceService.getContractVariables(byteCodeObjectDataList, contractState);
        Assert.assertTrue(contractVariables.containsKey("total"));
    }


    @Test
    public void get_balance_return_big_decimal() throws Exception {
        when(mockNodeApiExecService.getBalance(anyString())).thenReturn(new BigDecimal("19.5"));

        byte[] contractState = deploySmartContract().newContractState;
        ReturnValue rvBalance = executeSmartContract("getBalanceTest", new Variant[][]{{v_string("qwerty")}}, contractState);
        final BigDecimal bigDecimal = amountToBigDecimal(rvBalance.executeResults.get(0).result.getV_big_decimal()).setScale(1, RoundingMode.CEILING);
        Assert.assertEquals(new BigDecimal("19.5"), bigDecimal);
    }

    @Test
    public void multipleMethodCall() throws Exception {
        byte[] contractState = deploySmartContract().newContractState;

        ReturnValue singleCallResult = executeSmartContract("addTokens", new Variant[][]{{v_int(10)}}, contractState);

        ReturnValue multiplyCallResult = executeSmartContract(
                "addTokens",
                new Variant[][]{
                        {v_int(10)},
                        {v_int(10)},
                        {v_int(10)},
                        {v_int(10)}
                },
                contractState);

        assertNotEquals(singleCallResult.newContractState, multiplyCallResult.newContractState);

        singleCallResult = executeSmartContract("getTotal", contractState);

        TestCase.assertEquals(0, singleCallResult.executeResults.get(0).result.getV_int()); //fixme must be int
    }

    @Test
    public void compileClassCall() throws CompilationException {
        final List<ByteCodeObjectData> byteCodeObjectData = ceService.compileClass(sourceCode);
        Assert.assertNotNull(byteCodeObjectData);
        assertFalse(byteCodeObjectData.isEmpty());
    }


    @Test
    @DisplayName("getSeed must be return expectedValue")
    public void getSeedCallIntoSmartContract() throws Exception {
        var expectedValue = new byte[]{0xB, 0xA, 0xB, 0xE};

        var executeByteCodeResult = deploySmartContract();
        assertEquals(new APIResponse((byte) 0, "success"), executeByteCodeResult.executeResults.get(0).status);

        when(mockNodeApiExecService.getSeed(anyLong())).thenReturn(expectedValue);
        executeByteCodeResult = executeSmartContract("testGetSeed", executeByteCodeResult.newContractState);
        assertEquals(Arrays.asList(
                new Variant(V_BYTE, (byte) 0xB),
                new Variant(V_BYTE, (byte) 0xA),
                new Variant(V_BYTE, (byte) 0xB),
                new Variant(V_BYTE, (byte) 0xE)), executeByteCodeResult.executeResults.get(0).result.getV_array());
    }
}


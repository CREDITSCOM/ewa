package com.credits.service.contract;

import com.credits.general.thrift.generated.Variant;
import com.credits.service.ServiceTest;
import com.credits.thrift.ReturnValue;
import com.credits.thrift.utils.ContractUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TetrisContractTest extends ServiceTest {

    private byte[] bytecode;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        bytecode = compileSourceCode("/tetrisContractTest/Contract.java");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void tetrisContractTest() throws Exception {
        ReturnValue deployValue = ceService.execute(address, bytecode, null, null, null,500L);
        Assert.assertNotNull(deployValue);
        Assert.assertNotNull(deployValue.getContractState());

        ReturnValue executeValueGetAction = ceService.execute(address, bytecode, deployValue.getContractState(), "getCurrentAction", new Variant[][]{{}},500L);
        Assert.assertNotNull(executeValueGetAction);
        Assert.assertNotNull(executeValueGetAction.getContractState());
        Assert.assertEquals(1, executeValueGetAction.getVariantsList().get(0).getFieldValue());

        ReturnValue executeValueGetBalances = ceService.execute(address, bytecode, deployValue.getContractState(), "getBalance", new Variant[][]{{ContractUtils.mapObjectToVariant(0)}},500L);
        Assert.assertTrue(executeValueGetBalances.getVariantsList().get(0).getFieldValue() instanceof Map);
        Assert.assertEquals(20, ((Map<Variant, Variant>) executeValueGetBalances.getVariantsList().get(0).getFieldValue()).get(new Variant(Variant._Fields.V_STRING, "testKey2")).getFieldValue());

        ReturnValue executeGetSetOfString = ceService.execute(address, bytecode, deployValue.getContractState(), "getSetOfString", new Variant[][]{{}},500L);
        Assert.assertTrue(((Set<Variant>) executeGetSetOfString.getVariantsList().get(0).getFieldValue()).contains(new Variant(Variant._Fields.V_STRING, "Hello")));

        ReturnValue executeGetSetOfInteger = ceService.execute(address, bytecode, deployValue.getContractState(), "getSetOfInteger", new Variant[][]{{}},500L);
        Assert.assertTrue(((Set<Variant>) executeGetSetOfInteger.getVariantsList().get(0).getFieldValue()).contains(new Variant(Variant._Fields.V_I32, 555)));

        ReturnValue executeGetListOfDouble = ceService.execute(address, bytecode, deployValue.getContractState(), "getListOfDouble", new Variant[][]{{}},500L);
        Assert.assertTrue(((List<Variant>) executeGetListOfDouble.getVariantsList().get(0).getFieldValue()).contains(new Variant(Variant._Fields.V_DOUBLE, 5.55)));

        ReturnValue executeGetListOfString = ceService.execute(address, bytecode, deployValue.getContractState(), "getListOfString", new Variant[][]{{}},500L);
        Assert.assertTrue(((List<Variant>) executeGetListOfString.getVariantsList().get(0).getFieldValue()).contains(new Variant(Variant._Fields.V_STRING, "Hello")));

        ReturnValue executeGetDouble = ceService.execute(address, bytecode, deployValue.getContractState(), "getDouble", new Variant[][]{{}},500L);
        Assert.assertEquals(5.55, executeGetDouble.getVariantsList().get(0).getFieldValue());

        ReturnValue executeGetByte = ceService.execute(address, bytecode, deployValue.getContractState(), "getByte", new Variant[][]{{}},500L);
        Assert.assertEquals((byte) 5, executeGetByte.getVariantsList().get(0).getFieldValue());

        ReturnValue executeGetString = ceService.execute(address, bytecode, deployValue.getContractState(), "getString", new Variant[][]{{}},500L);
        Assert.assertEquals("Hello", executeGetString.getVariantsList().get(0).getFieldValue());
    }
}
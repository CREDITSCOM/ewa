package com.credits.service.contract;

import com.credits.service.ServiceTest;
import com.credits.thrift.ReturnValue;
import com.credits.thrift.Variant;
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
    public void tetrisContractTest() throws Exception {
        ReturnValue deployValue = ceService.execute(address, bytecode, null, null, null);
        Assert.assertNotNull(deployValue);
        Assert.assertNotNull(deployValue.getContractState());

        ReturnValue executeValueGetAction = ceService.execute(address, bytecode, deployValue.getContractState(), "getCurrentAction", null);
        Assert.assertNotNull(executeValueGetAction);
        Assert.assertNotNull(executeValueGetAction.getContractState());
        Assert.assertEquals(1, executeValueGetAction.getVariant().getFieldValue());

        ReturnValue executeValueGetBalances = ceService.execute(address, bytecode, deployValue.getContractState(), "getBalance", new String[]{"0"});
        Assert.assertTrue(executeValueGetBalances.getVariant().getFieldValue() instanceof Map);
        Assert.assertEquals(20, ((Map<Variant, Variant>) executeValueGetBalances.getVariant().getFieldValue()).get(new Variant(Variant._Fields.V_STRING, "testKey2")).getFieldValue());

        ReturnValue executeGetSetOfString = ceService.execute(address, bytecode, deployValue.getContractState(), "getSetOfString", null);
        Assert.assertTrue(((Set<Variant>) executeGetSetOfString.getVariant().getFieldValue()).contains(new Variant(Variant._Fields.V_STRING, "Hello")));

        ReturnValue executeGetSetOfInteger = ceService.execute(address, bytecode, deployValue.getContractState(), "getSetOfInteger", null);
        Assert.assertTrue(((Set<Variant>) executeGetSetOfInteger.getVariant().getFieldValue()).contains(new Variant(Variant._Fields.V_I32, 555)));

        ReturnValue executeGetListOfDouble = ceService.execute(address, bytecode, deployValue.getContractState(), "getListOfDouble", null);
        Assert.assertTrue(((List<Variant>) executeGetListOfDouble.getVariant().getFieldValue()).contains(new Variant(Variant._Fields.V_DOUBLE, 5.55)));

        ReturnValue executeGetListOfString = ceService.execute(address, bytecode, deployValue.getContractState(), "getListOfString", null);
        Assert.assertTrue(((List<Variant>) executeGetListOfString.getVariant().getFieldValue()).contains(new Variant(Variant._Fields.V_STRING, "Hello")));

        ReturnValue executeGetDouble = ceService.execute(address, bytecode, deployValue.getContractState(), "getDouble", null);
        Assert.assertEquals(5.55, executeGetDouble.getVariant().getFieldValue());

        ReturnValue executeGetByte = ceService.execute(address, bytecode, deployValue.getContractState(), "getByte", null);
        Assert.assertEquals((byte) 5, executeGetByte.getVariant().getFieldValue());

        ReturnValue executeGetString = ceService.execute(address, bytecode, deployValue.getContractState(), "getString", null);
        Assert.assertEquals("Hello", executeGetString.getVariant().getFieldValue());
    }
}
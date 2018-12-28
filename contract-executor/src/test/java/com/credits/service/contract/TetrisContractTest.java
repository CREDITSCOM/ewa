package com.credits.service.contract;

import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.VariantData;
import com.credits.general.pojo.VariantType;
import com.credits.general.thrift.generated.Variant;
import com.credits.service.ServiceTest;
import com.credits.thrift.ReturnValue;
import com.credits.thrift.utils.ContractExecutorUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TetrisContractTest extends ServiceTest {

    private List<ByteCodeObjectData> byteCodeObjectDataList;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        byteCodeObjectDataList = compileSourceCodeFromFile("/tetrisContractTest/Contract.java");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void tetrisContractTest() throws Exception {
        ReturnValue deployValue = ceService.execute(address, byteCodeObjectDataList, null, null, null,500L);
        Assert.assertNotNull(deployValue);
        Assert.assertNotNull(deployValue.getContractState());

        ReturnValue executeValueGetAction = ceService.execute(address, byteCodeObjectDataList, deployValue.getContractState(), "getCurrentAction", new Variant[][]{{}},500L);
        Assert.assertNotNull(executeValueGetAction);
        Assert.assertNotNull(executeValueGetAction.getContractState());
        Assert.assertEquals(1, executeValueGetAction.getVariantsList().get(0).getFieldValue());

        ReturnValue executeValueGetBalances = ceService.execute(address, byteCodeObjectDataList, deployValue.getContractState(), "getBalance", new Variant[][]{{
            ContractExecutorUtils.mapVariantDataToVariant(new VariantData(VariantType.INT, 0))}},500L);
        Assert.assertTrue(executeValueGetBalances.getVariantsList().get(0).getFieldValue() instanceof Map);
        Assert.assertEquals(20, ((Map<Variant, Variant>) executeValueGetBalances.getVariantsList().get(0).getFieldValue()).get(new Variant(Variant._Fields.V_STRING, "testKey2")).getFieldValue());

        ReturnValue executeGetSetOfString = ceService.execute(address, byteCodeObjectDataList, deployValue.getContractState(), "getSetOfString", new Variant[][]{{}},500L);
        Assert.assertTrue(((Set<Variant>) executeGetSetOfString.getVariantsList().get(0).getFieldValue()).contains(new Variant(Variant._Fields.V_STRING, "Hello")));

        ReturnValue executeGetSetOfInteger = ceService.execute(address, byteCodeObjectDataList, deployValue.getContractState(), "getSetOfInteger", new Variant[][]{{}},500L);
        Assert.assertTrue(((Set<Variant>) executeGetSetOfInteger.getVariantsList().get(0).getFieldValue()).contains(new Variant(Variant._Fields.V_INT_BOX, 555)));

        ReturnValue executeGetListOfDouble = ceService.execute(address, byteCodeObjectDataList, deployValue.getContractState(), "getListOfDouble", new Variant[][]{{}},500L);
        Assert.assertTrue(((List<Variant>) executeGetListOfDouble.getVariantsList().get(0).getFieldValue()).contains(new Variant(Variant._Fields.V_DOUBLE_BOX, 5.55)));

        ReturnValue executeGetListOfString = ceService.execute(address, byteCodeObjectDataList, deployValue.getContractState(), "getListOfString", new Variant[][]{{}},500L);
        Assert.assertTrue(((List<Variant>) executeGetListOfString.getVariantsList().get(0).getFieldValue()).contains(new Variant(Variant._Fields.V_STRING, "Hello")));

        ReturnValue executeGetDouble = ceService.execute(address, byteCodeObjectDataList, deployValue.getContractState(), "getDouble", new Variant[][]{{}},500L);
        Assert.assertEquals(5.55, executeGetDouble.getVariantsList().get(0).getFieldValue());

        ReturnValue executeGetByte = ceService.execute(address, byteCodeObjectDataList, deployValue.getContractState(), "getByte", new Variant[][]{{}},500L);
        Assert.assertEquals((byte) 5, executeGetByte.getVariantsList().get(0).getFieldValue());

        ReturnValue executeGetString = ceService.execute(address, byteCodeObjectDataList, deployValue.getContractState(), "getString", new Variant[][]{{}},500L);
        Assert.assertEquals("Hello", executeGetString.getVariantsList().get(0).getFieldValue());
    }
}
package com.credits.service.contract;

import com.credits.service.ServiceTest;
import com.credits.thrift.ReturnValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

        ReturnValue executeValue = ceService.execute(address, bytecode, deployValue.getContractState(), "getCurrentAction", null);
        Assert.assertNotNull(executeValue);
        Assert.assertNotNull(executeValue.getContractState());
        Assert.assertEquals(1, executeValue.getVariant().getFieldValue());
    }
}
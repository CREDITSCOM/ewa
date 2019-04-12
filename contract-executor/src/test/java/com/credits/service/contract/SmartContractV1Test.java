package com.credits.service.contract;


import com.credits.general.thrift.generated.Variant;
import com.credits.service.ServiceTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pojo.ReturnValue;

import static com.credits.general.thrift.generated.Variant.v_string;

public class SmartContractV1Test extends ServiceTest {

    public SmartContractV1Test() {
        super("/serviceTest/MyBasicStandard.java");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetBalance() throws Exception {
        byte[] contractState = deploySmartContract().newContractState;

        ReturnValue rvBalance = executeSmartContract("getBalance", new Variant[][] {{v_string("qwerty")}}, contractState);
        Assert.assertEquals(0, rvBalance.executeResults.get(0).result.getV_int());
    }


}


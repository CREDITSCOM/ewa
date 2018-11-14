package com.credits.wallet.desktop.utils;

import com.credits.general.exception.CreditsException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class SmartContractsUtilsTest {

    @Test
    public void generateSmartContractHashStateTest() throws CreditsException {
        String source =  "public class Contract extends SmartContract { public Contract() { total = 0; } }";
        String actual = SmartContractsUtils.generateSmartContractHashState(source.getBytes());
        Assert.assertEquals("A2CDBFDF50500999AEE29823F7D284D7", actual);
    }

}

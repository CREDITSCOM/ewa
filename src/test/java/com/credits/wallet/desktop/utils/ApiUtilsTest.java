package com.credits.wallet.desktop.utils;

import com.credits.common.exception.CreditsException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
public class ApiUtilsTest {
    private static Logger LOGGER = LoggerFactory.getLogger(ApiUtilsTest.class);

    @Test
    public void generateSmartContractHashStateTest() throws CreditsException {
        String source =  "public class Contract extends SmartContract { public Contract() { total = 0; } }";
        String actual = ApiUtils.generateSmartContractHashState(source.getBytes());
        Assert.assertEquals("A2CDBFDF50500999AEE29823F7D284D7", actual);
    }
}

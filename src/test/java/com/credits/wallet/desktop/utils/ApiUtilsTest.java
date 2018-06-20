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
    public void generateSmartContractHashStateTest() {
        String source =  "public class Contract extends SmartContract { public Contract() { total = 0; } }";
        try {
            LOGGER.info(ApiUtils.generateSmartContractHashState(source.getBytes()));
        } catch (CreditsException e) {
            Assert.assertNull(e);
        }
    }

    @Test
    public void generateTransactionInnerIdTest() {
        try {
            LOGGER.info(ApiUtils.generateTransactionInnerId());
        } catch (CreditsException e) {
            Assert.assertNull(e);
        }
    }
}

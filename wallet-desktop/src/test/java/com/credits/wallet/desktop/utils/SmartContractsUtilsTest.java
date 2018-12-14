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

    @Test
    public void generateSmartContractAddress() {

        //id 1


        //source
        // 07 14 49 2A 11 95 31 52 01 6A F0 3D FC 59 1C E7 0D 89 04 E8 9F 14 B4 BF 07 93 69 3F 2C CB 06 2F


        //expected address
        // 52 75 63 3C 20 21 D1 3A 83 03 6E B5 B5 2F 50 5D B9 31 FC 54 07 58 0F 1F 46 13 65 A8 39 D7 7F 60
    }
}

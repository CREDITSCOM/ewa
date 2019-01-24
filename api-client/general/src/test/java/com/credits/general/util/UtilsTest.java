package com.credits.general.util;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Rustem.Saidaliyev on 18.03.2018.
 */
public class UtilsTest {
    Logger LOGGER = LoggerFactory.getLogger(UtilsTest.class);

    @Test
    public void parseSubArrayTest() {
        byte[] bytes = new byte[64];
        for (int i = 0; i < 64; i++) {
            bytes[i] = (byte) i;
        }
        byte[] seedArray = Utils.parseSubArray(bytes, 0, 32);
        Assert.assertEquals(32, seedArray.length);
        Assert.assertEquals(0, seedArray[0]);
        Assert.assertEquals(31, seedArray[31]);

    }

    @Test
    public void concatenateArrayTest() {
        byte[] arr1 = new byte[32];
        byte[] arr2 = new byte[32];
        for (int i = 0; i < 32; i++) {
            arr1[i] = (byte) i;
            arr2[i] = (byte) i;
        }
        byte[] concatenatedArray = Utils.concatenateArrays(arr1, arr2);
        Assert.assertEquals(64, concatenatedArray.length);
        Assert.assertEquals(0, concatenatedArray[0]);
        Assert.assertEquals(31, concatenatedArray[31]);
        Assert.assertEquals(0, concatenatedArray[32]);
        Assert.assertEquals(31, concatenatedArray[63]);

    }

    @Test
    public void randomAlphaNumericTest() {
        String string = Utils.randomAlphaNumeric(29);
        Assert.assertEquals(29, string.length());
    }

    @Test
    @Ignore
    public void createActualOfferedMaxFeeTest() {
        double value = 0.0000000000001023D;
        Pair<Double, Short> actualOfferedMaxFeePair = Utils.createActualOfferedMaxFee(value);
        LOGGER.info(actualOfferedMaxFeePair.getLeft() + "");
        LOGGER.info(actualOfferedMaxFeePair.getRight() + "");
    }
}

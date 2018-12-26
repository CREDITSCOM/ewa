package com.credits.general.util;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Rustem.Saidaliyev on 29.11.2018.
 */
public class MathUtilsTest {
    Logger LOGGER = LoggerFactory.getLogger(MathUtilsTest.class);

    @Test
    @Ignore
    public void calActualFeeTest() {
        try {
            LOGGER.info(String.valueOf(MathUtils.calcActualFee(0.03)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

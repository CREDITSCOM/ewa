package com.credits.wallet.desktop.test;

import com.credits.wallet.desktop.utils.Convertor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Rustem.Saidaliyev on 29.01.2018.
 */
@SuppressWarnings("CanBeFinal")
public class ConvertorTest {
    private static Logger LOGGER = LoggerFactory.getLogger(ConvertorTest.class);
    @Test
    public void doubleToStringTest() {
        Double value = 0.126;
        LOGGER.info(Convertor.toString(value));
    }
}

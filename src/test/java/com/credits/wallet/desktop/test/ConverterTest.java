package com.credits.wallet.desktop.test;

import com.credits.wallet.desktop.utils.Converter;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * Created by Rustem.Saidaliyev on 29.01.2018.
 */
@SuppressWarnings("CanBeFinal")
public class ConverterTest {
    private static Logger LOGGER = LoggerFactory.getLogger(ConverterTest.class);
    @Test
    @Ignore
    public void doubleToStringTest() {
        Double value = 0.126;
        LOGGER.info(Converter.toString(value));
    }

    @Test
    public void base64EncodeDecodeTest() {

        byte[] data = "Hello World!!!".getBytes();

        try {
            for(byte b : data) {
                LOGGER.info(String.valueOf(b));
            }
            LOGGER.info("");
            LOGGER.info("");

            for(byte b : Converter.decodeFromBASE64(Converter.encodeToBASE64(data))) {
                LOGGER.info(String.valueOf(b));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

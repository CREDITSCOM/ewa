package com.credits.wallet.desktop.test;

import com.credits.wallet.desktop.utils.Converter;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;


/**
 * Created by Rustem.Saidaliyev on 29.01.2018.
 */
public class ConverterTest {
    private static Logger LOGGER = LoggerFactory.getLogger(ConverterTest.class);
    @Test
    public void doubleToStringTest() {
        Double value = 0.126;
        LOGGER.info(Converter.toString(value));
    }

    @Test
    public void decodeFromBASE64Test() {

        String text = "MC4CAQAwBQYDK2VwBCIEIB66QUnddK6yocOUWoPv4@";

        try {
            byte[] bytes = Converter.decodeFromBASE64(text);
            LOGGER.info(String.valueOf(bytes.length));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            assert false;
        }

    }

    @Test
    public void base64EncodeDecodeTest() {

        byte[] data1 = "Hello World!!!".getBytes();

        try {
            LOGGER.info(Arrays.toString(data1));
            LOGGER.info("");
            LOGGER.info("");
            byte[] data2 = Converter.decodeFromBASE64(Converter.encodeToBASE64(data1));
            LOGGER.info(Arrays.toString(data2));
            assert Arrays.equals(data1, data2);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            assert false;
        }

    }



}

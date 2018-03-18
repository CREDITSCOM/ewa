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
    //@Ignore
    public void decodeFromBASE64Test() {

        String text = "MC4CAQAwBQYDK2VwBCIEIB66QUnddK6yocOUWoPv4@";

        try {
            byte[] bytes = Converter.decodeFromBASE64(text);
            LOGGER.info(String.valueOf(bytes.length));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    @Ignore
    public void base64EncodeDecodeTest() {

        byte[] data = "Hello World!!!".getBytes();

        try {
            LOGGER.info(Arrays.toString(data));
            LOGGER.info("");
            LOGGER.info("");
            LOGGER.info(Arrays.toString(Converter.decodeFromBASE64(Converter.encodeToBASE64(data))));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}

package com.credits.wallet.desktop.test;

import com.credits.wallet.desktop.utils.Utils;
import com.credits.wallet.desktop.utils.Converter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by Rustem.Saidaliyev on 18.03.2018.
 */
public class UtilsTest {
    private static Logger LOGGER = LoggerFactory.getLogger(UtilsTest.class);

    @Test
    public void parseSubArrayTest() {
        try {
            byte[] publicKeyByteArr = Converter.decodeFromBASE64("f0j9xmzh1x8m5RvY4O8B6WGNigb2xVGQfPr7JGhgjDM=");
            byte[] privateKeyByteArr = Converter.decodeFromBASE64("6IOC+cSsndeFjx6eqEUoC1BVlo1gwGgdfK8f1O7IKYR/SP3GbOHXHyblG9jg7wHpYY2KBvbFUZB8+vskaGCMMw==");

            LOGGER.info("publicKeyByteArr  = {}", Arrays.toString(publicKeyByteArr));
            LOGGER.info("privateKeyByteArr = {}", Arrays.toString(privateKeyByteArr));

            byte[] seedByteArr = Utils.parseSubarray(privateKeyByteArr, 0, 32);

            LOGGER.info("seedByteAr        = {}", Arrays.toString(seedByteArr));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            assert false;
        }

    }

    @Test
    public void concatinateArrayTest() {
        try {
            byte[] publicKeyByteArr = Converter.decodeFromBASE64("f0j9xmzh1x8m5RvY4O8B6WGNigb2xVGQfPr7JGhgjDM=");
            byte[] privateKeyByteArr = Converter.decodeFromBASE64("6IOC+cSsndeFjx6eqEUoC1BVlo1gwGgdfK8f1O7IKYR/SP3GbOHXHyblG9jg7wHpYY2KBvbFUZB8+vskaGCMMw==");

            LOGGER.info("privateKeyByteArr = {}", Arrays.toString(privateKeyByteArr));
            LOGGER.info("publicKeyByteArr  = {}", Arrays.toString(publicKeyByteArr));

            byte[] concatinatedArray = Utils.concatinateArrays(publicKeyByteArr, privateKeyByteArr);

            LOGGER.info("concatinatedArray   {}", Arrays.toString(concatinatedArray));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            assert false;
        }

    }
}

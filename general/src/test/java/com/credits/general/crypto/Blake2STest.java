package com.credits.general.crypto;

import com.credits.general.crypto.exception.CryptoException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.credits.general.util.Converter.byteArrayToHex;

/**
 * Created by Rustem Saidaliyev on 27-Mar-18.
 */
public class Blake2STest {
    private static Logger LOGGER = LoggerFactory.getLogger(Blake2STest.class);

    @Test
    public void generateHashTest() throws CryptoException {
        byte[] hash = Blake2S.generateHash(4);
        Assert.assertEquals(4, hash.length);
        LOGGER.info("hash = {}", byteArrayToHex(hash));
    }
}

package com.credits.wallet.desktop.test;

import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Utils;
import com.credits.wallet.desktop.controller.Const;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.Converter;
import com.credits.wallet.desktop.utils.Ed25519;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.UUID;

/**
 * Created by Rustem Saidaliyev on 14-Mar-18.
 */
public class Ed25519Test {

    private static Logger LOGGER = LoggerFactory.getLogger(Ed25519Test.class);

    @Test
    @Ignore
    public void generateKeyPairTest() {

        KeyPair keyPair = Ed25519.generateKeyPair();
        LOGGER.info(Converter.encodeToBASE64(keyPair.getPublic().getEncoded()));
        LOGGER.info(Converter.encodeToBASE64(keyPair.getPrivate().getEncoded()));

        keyPair = Ed25519.generateKeyPair();
        LOGGER.info(Converter.encodeToBASE64(keyPair.getPublic().getEncoded()));
        LOGGER.info(Converter.encodeToBASE64(keyPair.getPrivate().getEncoded()));
    }

    @Test
    @Ignore
    public void signAndVerifyTest() {
        KeyPair keyPair = Ed25519.generateKeyPair();

        byte[] data = "Hello World!!!".getBytes();

        LOGGER.info("Public key: {}", Converter.encodeToBASE64(keyPair.getPublic().getEncoded()));
        LOGGER.info("Private key: {}", Converter.encodeToBASE64(keyPair.getPrivate().getEncoded()));

        try {
            byte[] signature = Ed25519.sign(data, keyPair.getPrivate());
            LOGGER.info("Signature: {}", Converter.encodeToBASE64(signature));

            Boolean verified = Ed25519.verify(data, signature, keyPair.getPublic());
            LOGGER.info("Verified: {}", verified);
        } catch (WalletDesktopException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void generateSignOfTransactionTest() {

        KeyPair keyPair = Ed25519.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        String hash = "00000000";
        String innerId = "1111111111111111111111111";
        Double amount = 0.0061D;
        String source = "source";
        String target = "target";
        String currency = "cs";

        try {
            String signature = Ed25519.generateSignOfTransaction(hash, innerId, source, target, amount, currency,
                    privateKey);
            LOGGER.info("signature = {}", signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

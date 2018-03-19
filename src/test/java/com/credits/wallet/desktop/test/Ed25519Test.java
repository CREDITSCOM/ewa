package com.credits.wallet.desktop.test;

import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Utils;
import com.credits.wallet.desktop.controller.Const;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.Converter;
import com.credits.wallet.desktop.utils.Ed25519;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Rustem Saidaliyev on 14-Mar-18.
 */
public class Ed25519Test {

    private static Logger LOGGER = LoggerFactory.getLogger(Ed25519Test.class);

    @Test
    public void generateKeyPairTest() {

        KeyPair keyPair = Ed25519.generateKeyPair();
        LOGGER.info(Converter.encodeToBASE64(keyPair.getPublic().getEncoded()));
        LOGGER.info(Converter.encodeToBASE64(keyPair.getPrivate().getEncoded()));
    }

    @Test
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
            assert verified;
        } catch (WalletDesktopException e) {
            assert false;
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
            assert false;
            e.printStackTrace();
        }
    }

    @Test
    public void bytesToPrivateKeyTest() {
        try {
            byte[] publicKeyByteArr = Converter.decodeFromBASE64("f0j9xmzh1x8m5RvY4O8B6WGNigb2xVGQfPr7JGhgjDM=");
            byte[] privateKeyByteArr = Converter.decodeFromBASE64("6IOC+cSsndeFjx6eqEUoC1BVlo1gwGgdfK8f1O7IKYR/SP3GbOHXHyblG9jg7wHpYY2KBvbFUZB8+vskaGCMMw==");

            LOGGER.info("publicKeyByteArr  = {}", Arrays.toString(publicKeyByteArr));
            LOGGER.info("privateKeyByteArr = {}", Arrays.toString(privateKeyByteArr));

            PublicKey publicKey = Ed25519.bytesToPublicKey(publicKeyByteArr);
            PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
        } catch (Exception e) {
            assert false;
            e.printStackTrace();
        }

    }


    @Test
    public void publicKeyToBytesTest() {

        String key1 = "f0j9xmzh1x8m5RvY4O8B6WGNigb2xVGQfPr7JGhgjDM=";

        try {
            byte[] bytes1 = Converter.decodeFromBASE64(key1);
            PublicKey publicKey = Ed25519.bytesToPublicKey(bytes1);
            byte[] bytes2 = Ed25519.publicKeyToBytes(publicKey);
            String key2 = Converter.encodeToBASE64(bytes2);
            LOGGER.info("bytes1 = {}", Arrays.toString(bytes1));
            LOGGER.info("bytes2 = {}", Arrays.toString(bytes2));
            LOGGER.info("key1 = {}", key1);
            LOGGER.info("key2 = {}", key2);
        } catch (IOException e) {
            assert false;
            e.printStackTrace();
        }
    }



    @Test
    public void privateKeyToBytesTest() {

        String key1 = "6IOC+cSsndeFjx6eqEUoC1BVlo1gwGgdfK8f1O7IKYR/SP3GbOHXHyblG9jg7wHpYY2KBvbFUZB8+vskaGCMMw==";
        LOGGER.info("key1 = {}", key1);
        try {
            byte[] bytes1 = Converter.decodeFromBASE64(key1);
            PrivateKey privateKey = Ed25519.bytesToPrivateKey(bytes1);
            byte[] bytes2 = Ed25519.privateKeyToBytes(privateKey);
            String key2 = Converter.encodeToBASE64(bytes2);
            LOGGER.info("bytes1 = {}", Arrays.toString(bytes1));
            LOGGER.info("bytes2 = {}", Arrays.toString(bytes2));
            LOGGER.info("key2 = {}", key2);
            assert Arrays.equals(bytes1, bytes2);
        } catch (IOException e) {
            assert false;
            e.printStackTrace();
        }
    }
}
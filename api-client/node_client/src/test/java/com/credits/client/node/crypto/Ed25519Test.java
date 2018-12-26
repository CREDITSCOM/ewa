package com.credits.client.node.crypto;

import com.credits.general.crypto.exception.CryptoException;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.exception.ConverterException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.utils.ThreadUtil;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.credits.general.util.Constants.ds;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by Igor Goryunov on 21.10.2018
 */
@SuppressWarnings("SpellCheckingInspection")
public class Ed25519Test {
    private static Logger LOGGER = LoggerFactory.getLogger(Ed25519Test.class);

    @Test
    public void encodeToBASE58Test() {
        int count = 0;
        for (int i = 0; i < 100; i++) {
            KeyPair keyPair = Ed25519.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            byte[] publicKeyBytes = Ed25519.publicKeyToBytes(publicKey);
            String publicKeyBASE58 = GeneralConverter.encodeToBASE58(publicKeyBytes);
            byte[] bytes = publicKeyBASE58.getBytes(StandardCharsets.US_ASCII);
            if (bytes.length != 44) {
                count++;
            }
        }
        LOGGER.info("count = {}", count);
    }

    @Test
    public void generateKeyPairTest() {
        KeyPair keyPair = Ed25519.generateKeyPair();
        Assert.assertNotNull(keyPair.getPublic());
        Assert.assertNotNull(keyPair.getPrivate());
        LOGGER.info(GeneralConverter.encodeToBASE64(keyPair.getPublic().getEncoded()));
        LOGGER.info(GeneralConverter.encodeToBASE64(keyPair.getPrivate().getEncoded()));
    }

    @Test
    public void signAndVerifyTest01() throws CryptoException {
        KeyPair keyPair = Ed25519.generateKeyPair();
        byte[] data = "Hello World!!!".getBytes();

        LOGGER.info("Public key: {}", GeneralConverter.encodeToBASE58(keyPair.getPublic().getEncoded()));
        LOGGER.info("Private key: {}", GeneralConverter.encodeToBASE58(keyPair.getPrivate().getEncoded()));

        byte[] signature = Ed25519.sign(data, keyPair.getPrivate());
        Assert.assertEquals(64, signature.length);
        LOGGER.info("Signature: {}", GeneralConverter.encodeToBASE58(signature));
        Boolean verified = Ed25519.verify(data, signature, keyPair.getPublic());
        Assert.assertTrue(verified);
        LOGGER.info("Verified: {}", verified);
    }

    @Test
    public void signAndVerifyTest02() throws ConverterException, CryptoException {
        byte[] data = "Hello World!!!".getBytes();

        PrivateKey privateKey = Ed25519.bytesToPrivateKey(
            GeneralConverter.decodeFromBASE58("4a1nut9D6v5AxKQMonKcttnDD2x1x9DmZikpfbxJh1S6R97NsXkSkZCQtGLm95TJ5V4emWAiN2BF2V4pn2zc7Jum"));
        PublicKey publicKey = Ed25519.bytesToPublicKey(GeneralConverter.decodeFromBASE58("6cZJCGMzDF1RzqzSu1fnwTNA9EYwnvzphyNzRTHLYcFy"));
        byte[] signature = Ed25519.sign(data, privateKey);
        Boolean verified = Ed25519.verify(data, signature, publicKey);
        LOGGER.info("Verified: {}", verified);
        Assert.assertTrue(verified);
    }

    @Test
    public void signAndVerifyInMultipleThreadsTest() {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(100);
            for (int i = 0; i < 100; i++) {
                executorService.submit(() -> {
                    try {
                        KeyPair keyPair = Ed25519.generateKeyPair();
                        byte[] data = "Hello World!!!".getBytes();

                        LOGGER.info("Public key: {}", GeneralConverter.encodeToBASE64(keyPair.getPublic().getEncoded()));
                        LOGGER.info("Private key: {}", GeneralConverter.encodeToBASE64(keyPair.getPrivate().getEncoded()));
                        try {
                            byte[] signature = Ed25519.sign(data, keyPair.getPrivate());
                            LOGGER.info("Signature: {}", GeneralConverter.encodeToBASE64(signature));

                            Boolean verified = Ed25519.verify(data, signature, keyPair.getPublic());
                            LOGGER.info("Verified: {}", verified);
                            assert verified;
                        } catch (CryptoException e) {
                            assert false;
                            e.printStackTrace();
                        }
                    } catch (Throwable e) {
                        LOGGER.info(e.getMessage());
                    }
                });
            }
            ThreadUtil.shutdownAndAwaitTermination(executorService, 1, MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void generateSignOfTransactionTest() throws ConverterException, CryptoException {
        KeyPair keyPair = Ed25519.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        String innerId = "1111111111111111111111111";
        String amountAsString = "1" + ds + "111111111111111";
        String balanceAsString = "1" + ds + "111111111111111";
        BigDecimal amount = new BigDecimal(amountAsString);
        BigDecimal balance = new BigDecimal(balanceAsString);
        String source = "account";
        String target = "target";
        byte currency = 1;

        String signature = Ed25519.generateSignOfTransaction(innerId, source, target, amount, balance, currency, privateKey);
        LOGGER.info("signature = {}", signature);
        Assert.assertNotEquals(0, signature.length());
    }

    @Test
    public void bytesToPrivateKeyTest() {
        byte[] publicKeyByteArr = GeneralConverter.decodeFromBASE64("f0j9xmzh1x8m5RvY4O8B6WGNigb2xVGQfPr7JGhgjDM=");
        byte[] privateKeyByteArr =
            GeneralConverter.decodeFromBASE64("6IOC+cSsndeFjx6eqEUoC1BVlo1gwGgdfK8f1O7IKYR/SP3GbOHXHyblG9jg7wHpYY2KBvbFUZB8+vskaGCMMw==");

        LOGGER.info("publicKeyByteArr  = {}", Arrays.toString(publicKeyByteArr));
        LOGGER.info("privateKeyByteArr = {}", Arrays.toString(privateKeyByteArr));

        PublicKey publicKey = Ed25519.bytesToPublicKey(publicKeyByteArr);
        PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
        Assert.assertNotNull(publicKey);
        Assert.assertNotNull(privateKey);
    }

    @Test
    public void publicKeyToBytesTest() {
        String key1 = "f0j9xmzh1x8m5RvY4O8B6WGNigb2xVGQfPr7JGhgjDM=";
        byte[] bytes1 = GeneralConverter.decodeFromBASE64(key1);
        PublicKey publicKey = Ed25519.bytesToPublicKey(bytes1);
        byte[] bytes2 = Ed25519.publicKeyToBytes(publicKey);
        String key2 = GeneralConverter.encodeToBASE64(bytes2);
        LOGGER.info("bytes1 = {}", Arrays.toString(bytes1));
        LOGGER.info("bytes2 = {}", Arrays.toString(bytes2));
        LOGGER.info("key1 = {}", key1);
        LOGGER.info("key2 = {}", key2);
        Assert.assertArrayEquals(bytes1, bytes2);
    }

    @Test
    public void privateKeyToBytesTest() {
        String key1 = "6IOC+cSsndeFjx6eqEUoC1BVlo1gwGgdfK8f1O7IKYR/SP3GbOHXHyblG9jg7wHpYY2KBvbFUZB8+vskaGCMMw==";
        byte[] bytes1 = GeneralConverter.decodeFromBASE64(key1);
        PrivateKey privateKey = Ed25519.bytesToPrivateKey(bytes1);
        byte[] bytes2 = Ed25519.privateKeyToBytes(privateKey);
        String key2 = GeneralConverter.encodeToBASE64(bytes2);
        LOGGER.info("bytes1 = {}", Arrays.toString(bytes1));
        LOGGER.info("bytes2 = {}", Arrays.toString(bytes2));
        LOGGER.info("key1 = {}", key1);
        LOGGER.info("key2 = {}", key2);
        Assert.assertArrayEquals(bytes1, bytes2);
    }
}
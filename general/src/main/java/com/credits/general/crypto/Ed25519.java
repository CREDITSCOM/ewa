package com.credits.general.crypto;

import com.credits.general.crypto.exception.CreditsCryptoException;
import com.credits.general.exception.GeneralClientException;
import com.credits.general.util.Utils;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.KeyPairGenerator;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;

/**
 * Утилита генерации публичных, приватных ключей, подписи
 * на базе ED25519
 * <p>
 * Created by Rustem Saidaliyev on 14-Mar-18.
 */
public class Ed25519 {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ed25519.class);

    private static final KeyPairGenerator KEY_PAIR_GENERATOR = new KeyPairGenerator();

    public static final String ED_25519 = "Ed25519";

    public static KeyPair generateKeyPair() {
        synchronized (KEY_PAIR_GENERATOR) {
            return KEY_PAIR_GENERATOR.generateKeyPair();
        }
    }

    public static byte[] sign(byte[] data, PrivateKey privateKey) throws CreditsCryptoException {

        EdDSAEngine edDSAEngine = new EdDSAEngine();
        try {
            edDSAEngine.initSign(privateKey);
            return edDSAEngine.signOneShot(data);
        } catch (InvalidKeyException | SignatureException e) {
            throw new CreditsCryptoException(e);
        }
    }

    public static Boolean verify(byte[] data, byte[] signature, PublicKey publicKey) throws CreditsCryptoException {

        EdDSAEngine edDSAEngine = new EdDSAEngine();
        try {
            edDSAEngine.initVerify(publicKey);
            return edDSAEngine.verifyOneShot(data, signature);
        } catch (InvalidKeyException | SignatureException e) {
            throw new CreditsCryptoException(e);
        }
    }

    public static PublicKey bytesToPublicKey(byte[] bytes) {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(ED_25519);
        EdDSAPublicKeySpec key = new EdDSAPublicKeySpec(bytes, spec);
        return new EdDSAPublicKey(key);
    }

    public static PrivateKey bytesToPrivateKey(byte[] bytes) {

        byte[] seedByteArr = Utils.parseSubarray(bytes, 0, 32); // seed
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(ED_25519);
        EdDSAPrivateKeySpec key = new EdDSAPrivateKeySpec(seedByteArr, spec);
        return new EdDSAPrivateKey(key);
    }

    //TODO implement that
    public static String generateSignOfTransaction(String innerId, String source, String target, BigDecimal amount,
        BigDecimal balance, byte currency, PrivateKey privateKey) throws GeneralClientException {

//        Amount amountValue = LevelDbClientConverter.bigDecimalToAmount(amount);
//
//        Integer amountIntegral = amountValue.getIntegral();
//        Long amountFraction = amountValue.getFraction();
//
//        Amount balanceValue = LevelDbClientConverter.bigDecimalToAmount(balance);
//
//        Integer balanceIntegral = balanceValue.getIntegral();
//        Long balanceFraction = balanceValue.getFraction();
//
//        String transaction =
//            String.format("%s|%s|%s|%s:%s|%s:%s|%s", innerId, source, target, Converter.toString(amountIntegral),
//                Converter.toString(amountFraction), Converter.toString(balanceIntegral),
//                Converter.toString(balanceFraction), currency);
//
//        LOGGER.debug("Signing the message [{}]", transaction);
//        byte[] signature;
//        try {
//            signature = Ed25519.sign(transaction.getBytes(StandardCharsets.US_ASCII), privateKey);
//        } catch (CreditsCryptoException e) {
//            throw new LevelDbClientException(e);
//        }

//        return Converter.encodeToBASE58(signature);
        return "";
    }

    public static byte[] publicKeyToBytes(PublicKey publicKey) {
        EdDSAPublicKey edDSAPublicKey = (EdDSAPublicKey) publicKey;
        return edDSAPublicKey.getAbyte();
    }

    public static byte[] privateKeyToBytes(PrivateKey privateKey) {
        EdDSAPrivateKey edDSAPrivateKey = (EdDSAPrivateKey) privateKey;
        return Utils.concatinateArrays(edDSAPrivateKey.getSeed(), edDSAPrivateKey.getAbyte());
    }
}
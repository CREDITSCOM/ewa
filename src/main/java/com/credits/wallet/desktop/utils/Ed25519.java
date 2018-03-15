package com.credits.wallet.desktop.utils;

import com.credits.leveldb.client.thrift.Amount;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.KeyPairGenerator;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

import java.security.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Утилита генерации публичных, приватных ключей, подписи
 * на базе ED25519
 *
 * Created by Rustem Saidaliyev on 14-Mar-18.
 */
public class Ed25519 {

    public static final String ED_25519 = "Ed25519";

    public static KeyPairGenerator KEY_PAIR_GENERATOR = new KeyPairGenerator();

    public static KeyPair generateKeyPair() {

        return KEY_PAIR_GENERATOR.generateKeyPair();
    }

    public static byte[] sign(byte[] data, PrivateKey privateKey) throws WalletDesktopException {

        EdDSAEngine edDSAEngine = new EdDSAEngine();
        try {
            edDSAEngine.initSign(privateKey);
            return edDSAEngine.signOneShot(data);
        } catch (InvalidKeyException e) {
            throw new WalletDesktopException(e);
        } catch (SignatureException e) {
            throw new WalletDesktopException(e);
        }
    }

    public static Boolean verify(byte[] data, byte[] signature, PublicKey publicKey) throws WalletDesktopException {

        EdDSAEngine edDSAEngine = new EdDSAEngine();
        try {
            edDSAEngine.initVerify(publicKey);
            return edDSAEngine.verifyOneShot(data, signature);
        } catch (InvalidKeyException e) {
            throw new WalletDesktopException(e);
        } catch (SignatureException e) {
            throw new WalletDesktopException(e);
        }
    }

    public static PublicKey bytesToPublicKey(byte[] bytes) {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(ED_25519);
        EdDSAPublicKeySpec key = new EdDSAPublicKeySpec(bytes, spec);
        return new EdDSAPublicKey(key);
    }

    public static PrivateKey bytesToPrivateKey(byte[] bytes) {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(ED_25519);
        EdDSAPrivateKeySpec key = new EdDSAPrivateKeySpec(bytes, spec);
        return new EdDSAPrivateKey(key);
    }

    public static String generateSignOfTransaction(
            String hash,
            String innerId,
            String source,
            String target,
            Double amount,
            String currency,
            PrivateKey privateKey
    ) throws Exception {

        Amount amountValue = com.credits.leveldb.client.util.Converter.doubleToAmount(amount);

        Integer amountIntegral = amountValue.getIntegral();
        Long amountFraction = amountValue.getFraction();

        String transaction = String.format("%s|%s|%s|%s|%s:%s|%s",
                hash,
                innerId,
                source,
                target,
                Converter.toString(amountIntegral),
                Converter.toString(amountFraction),
                currency
        );

        byte[] signature = Ed25519.sign(transaction.getBytes(), privateKey);

        return Converter.encodeToBASE64(signature);
    }

}
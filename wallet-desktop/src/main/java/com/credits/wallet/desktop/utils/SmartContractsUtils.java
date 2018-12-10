package com.credits.wallet.desktop.utils;

import com.credits.client.node.crypto.Ed25519;
import com.credits.general.crypto.Md5;
import com.credits.general.exception.CreditsException;

import java.security.KeyPair;
import java.security.PublicKey;

import static com.credits.general.util.GeneralConverter.byteArrayToHex;

public class SmartContractsUtils {
    public static byte[] generateSmartContractAddress() {
        KeyPair keyPair = Ed25519.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        return Ed25519.publicKeyToBytes(publicKey);
    }

    public static String generateSmartContractHashState(byte[] byteCode) throws CreditsException {
        byte[] hashBytes = Md5.encrypt(byteCode);
        return byteArrayToHex(hashBytes);
    }
}

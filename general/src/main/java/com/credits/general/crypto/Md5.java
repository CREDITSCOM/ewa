package com.credits.general.crypto;

import com.credits.general.crypto.exception.CreditsCryptoException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Rustem Saidaliyev on 17.05.2018.
 */
public class Md5 {
    public static byte[] encrypt(byte[] bytes) throws CreditsCryptoException {

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new CreditsCryptoException(e);
        }
        digest.update(bytes);
        return digest.digest();
    }
}

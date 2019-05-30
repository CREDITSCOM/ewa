package com.credits.general.crypto;

import com.credits.general.crypto.exception.CryptoException;
import org.bouncycastle.crypto.digests.Blake2sDigest;


public class Blake2S {

    public static byte[] generateHash(byte[] hashData) throws CryptoException {
        byte[] outDigest = new byte[32];
        Blake2sDigest blake2sDigest = new Blake2sDigest();
        blake2sDigest.update(hashData, 0, hashData.length);
        blake2sDigest.doFinal(outDigest, 0);
        return outDigest;
    }
}

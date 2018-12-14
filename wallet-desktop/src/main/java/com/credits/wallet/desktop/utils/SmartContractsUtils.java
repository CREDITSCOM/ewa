package com.credits.wallet.desktop.utils;

import com.credits.client.node.crypto.Ed25519;
import com.credits.general.crypto.Md5;
import com.credits.general.exception.CreditsException;
import org.apache.commons.lang3.ArrayUtils;

import java.security.KeyPair;
import java.security.PublicKey;

import static com.credits.general.crypto.Blake2S.generateHash;
import static com.credits.general.util.GeneralConverter.byteArrayToHex;
import static com.credits.general.util.GeneralConverter.toByteArray;
import static org.apache.commons.lang3.ArrayUtils.*;

public class SmartContractsUtils {
    public static byte[] generateSmartContractAddress(byte[] deployerAddress, long transactionId, byte[] bytecode) {
        byte[] seed = addAll(deployerAddress, toByteArray(transactionId));
        seed = addAll(seed, bytecode);
        return generateHash(seed);
    }

    public static String generateSmartContractHashState(byte[] byteCode) throws CreditsException {
        byte[] hashBytes = Md5.encrypt(byteCode);
        return byteArrayToHex(hashBytes);
    }
}

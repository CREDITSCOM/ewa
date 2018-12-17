package com.credits.wallet.desktop.utils;

import com.credits.general.crypto.Md5;
import com.credits.general.exception.CreditsException;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.credits.general.crypto.Blake2S.generateHash;
import static com.credits.general.util.GeneralConverter.byteArrayToHex;
import static com.credits.general.util.GeneralConverter.toByteArray;
import static com.credits.general.util.GeneralConverter.toByteArrayLittleEndian;
import static org.apache.commons.lang3.ArrayUtils.addAll;

public class SmartContractsUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(SmartContractsUtils.class);
    private static byte[] bytes;


    public static byte[] generateSmartContractAddress(byte[] deployerAddress, long transactionId, byte[] bytecode) {

        bytes = toByteArray(transactionId);

        byte[] sliceId = Arrays.copyOfRange(toByteArray(transactionId), 2, 8);
        ArrayUtils.reverse(sliceId);

        byte[] seed = addAll(deployerAddress, toByteArrayLittleEndian(sliceId,sliceId.length));
        seed = addAll(seed, bytecode);
        seed = toByteArrayLittleEndian(seed, seed.length);
        LOGGER.info("Generate smart contract address:\n");

        for(Byte b: seed){
            System.out.print(((0xff) & b) + ", ");
        }
        byte[] bytes = generateHash(seed);
        return bytes;
    }

    public static String generateSmartContractHashState(byte[] byteCode) throws CreditsException {
        byte[] hashBytes = Md5.encrypt(byteCode);
        return byteArrayToHex(hashBytes);
    }
}

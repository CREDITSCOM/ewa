package com.credits.service.contract.validator;

import com.credits.exception.ContractExecutorException;
import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.SmartContractData;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ByteCodeValidator {

    private static final String HASH_ALGO = "MD5";

    public static void validateBytecode(byte[] bytecode, SmartContractData contractToValidate) throws ContractExecutorException {
        String expectedHashState;
        try {
            expectedHashState = ByteCodeValidator.encrypt(bytecode);
        } catch (CreditsException e) {
            throw new ContractExecutorException("Internal error", e);
        }

        if (!expectedHashState.equals(contractToValidate.getSmartContractDeployData().getHashState())) {
            throw new ContractExecutorException("Unknown contract");
        }
    }

    private static String encrypt(byte[] bytes) throws CreditsException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(HASH_ALGO);
        } catch (NoSuchAlgorithmException e) {
            throw new CreditsException(e);
        }
        digest.update(bytes);
        return Hex.encodeHexString(digest.digest());

    }
}

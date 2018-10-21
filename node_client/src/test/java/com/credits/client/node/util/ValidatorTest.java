package com.credits.client.node.util;

import com.credits.general.util.exception.ConverterException;
import org.junit.Test;

import static com.credits.client.node.util.Validator.validateToAddress;
import static com.credits.client.node.util.Validator.validateTransactionHash;

/**
 * Created by Rustem Saidaliyev on 20-Mar-18.
 */
@SuppressWarnings("SpellCheckingInspection")
public class ValidatorTest {

    @Test
    public void validateTransactionHashTest() throws ConverterException {
        String hash = "8iif6oqo";
        validateTransactionHash(hash);
    }

    @Test
    public void validateToAddressTest() throws ConverterException {
        String toAddress = "HMXHATk9fRJrdHS1rkge5zgwHWXCPAnjUmiSxsGnBcq4";
        validateToAddress(toAddress);
    }
}

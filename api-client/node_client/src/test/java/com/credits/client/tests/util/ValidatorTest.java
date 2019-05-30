package com.credits.client.tests.util;

import com.credits.client.node.exception.NodeClientException;
import com.credits.general.util.exception.ConverterException;
import org.junit.Test;

import static com.credits.client.node.util.Validator.validateToAddress;
import static com.credits.client.node.util.Validator.validateTransactionHash;


@SuppressWarnings("SpellCheckingInspection")
public class ValidatorTest {

    @Test
    public void validateTransactionHashTest() throws ConverterException {
        String hash = "8iif6oqo";
        try {
            validateTransactionHash(hash);
        } catch (NodeClientException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void validateToAddressTest() throws ConverterException {
        String toAddress = "HMXHATk9fRJrdHS1rkge5zgwHWXCPAnjUmiSxsGnBcq4";
        try {
            validateToAddress(toAddress);
        } catch(NodeClientException e) {
            e.printStackTrace();
            assert false;
        }

    }
}

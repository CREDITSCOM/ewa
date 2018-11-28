package com.credits.serialise;

import com.credits.exception.ContractExecutorException;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;

import static com.credits.serialize.Serializer.deserialize;
import static com.credits.serialize.Serializer.serialize;

public class SerializerTest {
    protected final String address = "1a2b3c";

    @Test
    public void serialize_deserialize() throws ContractExecutorException {
        Contract smartContract = new Contract();
        smartContract.addTotal(100);
        byte[] contractState = serialize(address, smartContract);


        Contract desObject = (Contract) deserialize(contractState, getClass().getClassLoader());
        Assert.assertEquals(101, desObject.getTotal());
    }

    static class Contract implements Serializable {
        private int total = 1;

        public void addTotal(int amount) {
            total += amount;
        }

        public int getTotal() {
            return total;
        }
    }
}
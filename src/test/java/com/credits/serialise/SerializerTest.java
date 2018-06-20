package com.credits.serialise;

import com.credits.exception.ContractExecutorException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.Serializable;

import static com.credits.serialise.Serializer.deserialize;
import static com.credits.serialise.Serializer.getSerFile;
import static com.credits.serialise.Serializer.serialize;
import static java.io.File.separator;

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
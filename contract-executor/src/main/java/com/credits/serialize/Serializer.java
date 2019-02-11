package com.credits.serialize;

import com.credits.exception.ContractExecutorException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static java.io.File.separator;

public class Serializer {

    private static final String serFileName = "Contract.out";
    private final static String SER_SOURCE_FOLDER_PATH = System.getProperty("user.dir") + separator + "credits" + separator;

    public static Object deserialize(byte[] contractState, ClassLoader classLoader) throws ContractExecutorException {
        Object instance;

        try (ObjectInputStream ous = new ObjectInputStreamWithClassLoader(new ByteArrayInputStream(contractState), classLoader)) {
            instance = ous.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ContractExecutorException("Cannot getObject smart contract instance. " + e);
        }
        return instance;
    }

    public static byte[] serialize(Object instance) throws ContractExecutorException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream ous = new ObjectOutputStream(baos)) {
            ous.writeObject(instance);
        } catch (IOException e) {
            throw new ContractExecutorException("Cannot serialize smart contract instance. " + e);
        }
        return baos.toByteArray();
    }

    public static File getSerFile(String address) {
        File sourcePath = new File(SER_SOURCE_FOLDER_PATH + address);
        File[] files = sourcePath.listFiles();
        if (files != null && files.length > 0) {
            return new File(SER_SOURCE_FOLDER_PATH + separator + address + separator + serFileName);
        }
        return null;
    }
}

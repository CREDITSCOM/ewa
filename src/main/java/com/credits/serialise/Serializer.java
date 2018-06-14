package com.credits.serialise;

import com.credits.exception.ContractExecutorException;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static java.io.File.*;

public class Serializer {

    private static final String serFileName = "Contract.out";
    private final static String SER_SOURCE_FOLDER_PATH = System.getProperty("user.dir") + separator + "credits" + separator;

    public static Object deserialize(File serFile, ClassLoader classLoader) throws ContractExecutorException {
        Object instance;

        try (ObjectInputStream ous = new ObjectInputStreamWithClassLoader(new FileInputStream(serFile), classLoader)) {
            instance = ous.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ContractExecutorException("Cannot load smart contract instance. " + e);
        }
        return instance;
    }

    public static void serialize(String address, Object instance) throws ContractExecutorException {
        String objFile = SER_SOURCE_FOLDER_PATH + address + separator + serFileName;
        File serFile = new File(objFile);
        serFile.getParentFile().mkdirs();
        try (ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(serFile))) {
            ous.writeObject(instance);
        } catch (IOException e) {
            throw new ContractExecutorException("Cannot save smart contract instance. " + e);
        }
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

package com.credits.serialise;

import com.credits.exception.ContractExecutorException;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Serializer {

    private final static String SER_EXT = "out";
    private final static String SER_SOURCE_FOLDER_PATH = System.getProperty("user.dir") + File.separator + "credits";

    public static Object deserialize(File serFile) throws ContractExecutorException {
        Object instance;

        try (ObjectInputStream ous = new ObjectInputStream(new FileInputStream(serFile))){
            instance = ous.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ContractExecutorException("Cannot load smart contract instance. " + e);
        }
        return instance;
    }

    public static void serialize(File serFile, Object instance) throws ContractExecutorException {
        try (ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(serFile))){
            ous.writeObject(instance);
        } catch (IOException e) {
            throw new ContractExecutorException("Cannot save smart contract instance. " + e);
        }
    }

    public static File getSerFile(String address) {
        File sourcePath = new File(SER_SOURCE_FOLDER_PATH + File.separator + address);
        String fileName = sourcePath.listFiles()[0].getName();
        String serFileName = FilenameUtils.getBaseName(fileName) + "." + SER_EXT;
        return new File(SER_SOURCE_FOLDER_PATH + File.separator + address +
            File.separator + serFileName);
    }
}

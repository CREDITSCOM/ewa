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

    public static void deserialize(File serFile, Boolean methodIsStatic, Object instance, List<Field> fields) throws ContractExecutorException {
        Map<String, Object> deserFields;

        try (ObjectInputStream ous = new ObjectInputStream(new FileInputStream(serFile))){
            deserFields = (Map<String, Object>) ous.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ContractExecutorException("Cannot load saved class fields. " + e);
        }

        if (fields != null && fields.size() != 0) {
            for (Field field : fields) {
                try {
                    if ((!methodIsStatic || Modifier.isStatic(field.getModifiers())) && deserFields.containsKey(field.getName())) {
                        field.setAccessible(true);
                        field.set(instance, deserFields.get(field.getName()));
                    }
                } catch (IllegalAccessException e) {
                    throw new ContractExecutorException("Cannot insert class fields. " + e);
                }
            }
        }
    }

    public static void serialize(File serFile, Object instance, List<Field> fields) throws ContractExecutorException {
        HashMap<String, Object> serFields = new HashMap<>();
        if (fields != null && fields.size() != 0) {
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    serFields.put(field.getName(), field.get(instance));
                } catch (IllegalAccessException e) {
                    throw new ContractExecutorException("Cannot save class fields. " + e);
                }
            }
        }

        try (ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(serFile))){
            ous.writeObject(serFields);
        } catch (IOException e) {
            throw new ContractExecutorException("Cannot save class fields. " + e);
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

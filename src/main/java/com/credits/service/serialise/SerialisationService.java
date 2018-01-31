package com.credits.service.serialise;

import com.credits.exception.ContractExecutorException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

@Component
public class SerialisationService {

    private final static String SER_EXT = "out";
    private final static String SER_SOURCE_FOLDER_PATH = System.getProperty("user.dir") + File.separator + "credits";

    public void deserialize(File serFile, Boolean methodIsStatic, Object instance, Class<?> clazz) throws ContractExecutorException {
        Map<String, Object> deserFields;

        try (ObjectInputStream ous = new ObjectInputStream(new FileInputStream(serFile))){
            deserFields = (Map<String, Object>) ous.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ContractExecutorException("Cannot load saved class fields. " + e);
        }

        Field[] fieldsFromClass = clazz.getDeclaredFields();
        if (fieldsFromClass != null && fieldsFromClass.length != 0) {
            for (Field field : fieldsFromClass) {
                try {
                    if (!methodIsStatic || Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        field.set(instance, deserFields.get(field.getName()));
                    }
                } catch (IllegalAccessException e) {
                    throw new ContractExecutorException("Cannot insert class fields. " + e);
                }
            }
        }
    }

    public void serialize(File serFile, Boolean methodIsStatic, Object instance, Class<?> clazz) throws ContractExecutorException {
        HashMap<String, Object> serFields = new HashMap<>();
        Field[] fieldsForSer = clazz.getDeclaredFields();
        if (fieldsForSer != null && fieldsForSer.length != 0) {
            for (Field field : fieldsForSer) {
                try {
                    if (!methodIsStatic || Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        serFields.put(field.getName(), field.get(instance));
                    }
                } catch (IllegalAccessException e) {
                    throw new ContractExecutorException("Cannot load saved class fields. " + e);
                }
            }
        }

        try (ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(serFile))){
            ous.writeObject(serFields);
        } catch (IOException e) {
            throw new ContractExecutorException("Cannot save class fields. " + e);
        }
    }

    public File getSerFile(String address) {
        File sourcePath = new File(SER_SOURCE_FOLDER_PATH + File.separator + address);
        String fileName = sourcePath.listFiles()[0].getName();
        String serFileName = FilenameUtils.getBaseName(fileName) + "." + SER_EXT;
        return new File(SER_SOURCE_FOLDER_PATH + File.separator + address +
            File.separator + serFileName);
    }
}

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
import java.util.Map;

public class Serializer {

    private final static String SER_EXT = "out";
    private final static String SER_SOURCE_FOLDER_PATH = System.getProperty("user.dir") + File.separator + "credits";

    public static void deserialize(File serFile, Boolean methodIsStatic, Object instance, Class<?> clazz) throws ContractExecutorException {
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

    public static void serialize(File serFile, Boolean methodIsStatic, Object instance, Class<?> clazz) throws ContractExecutorException {
        HashMap<String, Object> serFields = new HashMap<>();
        Field[] fieldsForSer = clazz.getDeclaredFields();
        if (fieldsForSer != null && fieldsForSer.length != 0) {
            for (Field field : fieldsForSer) {
                try {
                    String clas = field.getType().getName();
                    if ((!methodIsStatic || Modifier.isStatic(field.getModifiers())) && (isSupportedType(clas))) {
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

    public static File getSerFile(String address) {
        File sourcePath = new File(SER_SOURCE_FOLDER_PATH + File.separator + address);
        String fileName = sourcePath.listFiles()[0].getName();
        String serFileName = FilenameUtils.getBaseName(fileName) + "." + SER_EXT;
        return new File(SER_SOURCE_FOLDER_PATH + File.separator + address +
            File.separator + serFileName);
    }

    private static Boolean isSupportedType(String clas) {
        switch (clas) {
            case "byte":
                return true;
            case "short":
                return true;
            case "int":
                return true;
            case "long":
                return true;
            case "float":
                return true;
            case "double":
                return true;
            case "char":
                return true;
            case "boolean":
                return true;
            case "java.lang.Byte":
                return true;
            case "java.lang.Short":
                return true;
            case "java.lang.Integer":
                return true;
            case "java.lang.Long":
                return true;
            case "java.lang.Float":
                return true;
            case "java.lang.Double":
                return true;
            case "java.lang.Character":
                return true;
            case "java.lang.Boolean":
                return true;
            case "java.lang.String":
                return true;
            default:
                return false;
        }
    }
}

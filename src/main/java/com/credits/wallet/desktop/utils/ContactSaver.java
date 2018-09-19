package com.credits.wallet.desktop.utils;

import com.credits.leveldb.client.data.SmartContractData;
import com.credits.wallet.desktop.AppState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContactSaver {

    public static final String fileDir = "contracts";
    public static final String fileName = "contact.ser";
    private static final Logger LOGGER = LoggerFactory.getLogger(ContactSaver.class);

    public static boolean serialize(Map collection) {
        try {
            String fullFileName = getFullFileName();
            Path filePath = Paths.get(System.getProperty("user.dir") + File.separator + fileDir);
            if (!Files.exists(filePath)) {
                LOGGER.info("Create fileDir");
                Files.createDirectories(filePath);
                LOGGER.info("FileDir was created");
            }
            String fullFilePath = filePath + File.separator + fullFileName;
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fullFilePath))) {
                oos.writeObject(collection);
            }
            LOGGER.info("Set was serialized");
            return true;
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            return false;
        }
    }

    public static Map<String, SmartContractData> deserialize() {
        try {
            String fullFileName = getFullFileName();
            Path filePath = Paths.get(System.getProperty("user.dir") + File.separator + fileDir);
            String fullFilePath = filePath + File.separator + fullFileName;

            Map map;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fullFilePath))) {
                map = (Map) ois.readObject();
            }
            LOGGER.info("Set was deserialized");
            return map;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.info(e.getMessage());
            return null;
        }
    }

    private static String getFullFileName() {
        String fullFileName;
        if(AppState.account !=null) {
            fullFileName = AppState.account + "contact.ser";
        } else {
            fullFileName = fileName;
        }
        return fullFileName;
    }
}

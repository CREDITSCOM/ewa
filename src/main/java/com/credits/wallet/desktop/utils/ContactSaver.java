package com.credits.wallet.desktop.utils;

import com.credits.leveldb.client.data.SmartContractData;
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
import java.util.Map;

import static com.credits.wallet.desktop.AppState.account;
import static java.io.File.separator;

public class ContactSaver {

    public static final String fileName = "obj.ser";
    public static final Path cacheDirectory = Paths.get(System.getProperty("user.dir") + separator + "cache" );
    public static final Path accountDirectory = Paths.get(cacheDirectory + separator + account );
    private static final Logger LOGGER = LoggerFactory.getLogger(ContactSaver.class);

    public static boolean serialize(Map collection) {
        try {
            Files.createDirectories(accountDirectory);
            Path serObjectFile = getSerializedObjectPath();
            File serFile = serObjectFile.toFile();
            if(!serFile.exists()){
                serFile.createNewFile();
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serObjectFile.toString()))) {
                oos.writeObject(collection);
                oos.flush();
            }
            LOGGER.info("Object was serialized");
            return true;
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            return false;
        }
    }

    public static Map<String, SmartContractData> deserialize() {
        try {
            Map map;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getSerializedObjectPath().toString()))) {
                map = (Map) ois.readObject();
            }
            LOGGER.info("Object was deserialized");
            return map;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.info(e.getMessage());
            return null;
        }
    }

    static Path getSerializedObjectPath() {
        return Paths.get(getAccountDirectory() + separator + fileName);
    }

    static Path getAccountDirectory(){
        return Paths.get(cacheDirectory + separator + account);
    }
}

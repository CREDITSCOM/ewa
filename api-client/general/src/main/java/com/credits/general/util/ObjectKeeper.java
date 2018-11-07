package com.credits.general.util;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static com.credits.general.util.CriticalSection.doSafe;
import static java.io.File.separator;

public class ObjectKeeper<T extends ConcurrentHashMap> {

    static final Path cacheDirectory = Paths.get(System.getProperty("user.dir") + separator + "cache");
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectKeeper.class);
    private final String objectFileName;
    private final String account;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile T storedObject;

    public ObjectKeeper(String account, String objectName) {
        this.account = account;
        this.objectFileName = objectName + ".ser";
    }

    public void keepObject(T object) {
        doSafe(() -> serialize(object), lock);
    }

    public T getKeptObject() {
        if (storedObject == null) {
            return doSafe(this::deserialize, lock);
        }
        return storedObject;
    }

    public T getKeptObject(Function<T> createIfAbsent) {
        if (storedObject == null) {
            if(doSafe(this::deserialize, lock) == null){
                return createIfAbsent.apply();
            }
        }
        return storedObject;
    }

    public void modify(Modifier changeObject) {
        doSafe(() -> keepObject(changeObject.modify(getKeptObject())), lock);
    }

    Path getSerializedObjectPath() {
        return Paths.get(getAccountDirectory() + separator + objectFileName);
    }

    Path getAccountDirectory() {
        return Paths.get(cacheDirectory + separator + account);
    }

    private void serialize(T object) {
        try {
            Files.createDirectories(getAccountDirectory());
            Path serObjectFile = getSerializedObjectPath();
            File serFile = serObjectFile.toFile();
            if (!serFile.exists()) {
                if(!serFile.createNewFile()){
                    throw new IOException("can't create new file");
                }
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serObjectFile.toString()))) {
                oos.writeObject(object);
                storedObject = object;
            }
        }catch (SecurityException | IOException e) {
            LOGGER.error("Object can't serialized. Reason: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private T deserialize() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getSerializedObjectPath().toString()))) {
            return storedObject = (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    public abstract class Modifier {
        public abstract T modify(T restoredObject);
    }
}

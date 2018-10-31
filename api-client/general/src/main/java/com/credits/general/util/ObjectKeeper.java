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

import static java.io.File.separator;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ObjectKeeper<T extends ConcurrentHashMap> {

    static final Path cacheDirectory = Paths.get(System.getProperty("user.dir") + separator + "cache");
    static final Integer TRY_LOCK_TIMEOUT = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectKeeper.class);
    private final String objectFileName;
    private final String account;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile T storedObject;

    public ObjectKeeper(String account, String objectName) {
        this.account = account;
        this.objectFileName = objectName + ".ser";
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void serialize(T object) {
        try {
            lock.tryLock(TRY_LOCK_TIMEOUT, SECONDS);
            Files.createDirectories(getAccountDirectory());
            Path serObjectFile = getSerializedObjectPath();
            File serFile = serObjectFile.toFile();
            if (!serFile.exists()) {
                serFile.createNewFile();
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serObjectFile.toString()))) {
                oos.writeObject(object);
                storedObject = object;
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void deserializeThenSerialize(Modifier changeObject) {
        try {
            lock.tryLock(TRY_LOCK_TIMEOUT, SECONDS);
            serialize(changeObject.modify(deserialize()));
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    Path getSerializedObjectPath() {
        return Paths.get(getAccountDirectory() + separator + objectFileName);
    }

    Path getAccountDirectory() {
        return Paths.get(cacheDirectory + separator + account);
    }

    @SuppressWarnings("unchecked")
    public T deserialize() {
        if (storedObject == null) {
            try {
                lock.tryLock(TRY_LOCK_TIMEOUT, SECONDS);
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getSerializedObjectPath().toString()))) {
                    storedObject = (T) ois.readObject();
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                LOGGER.error(e.getMessage());
                return null;
            } finally {
                lock.unlock();
            }
        }
        return storedObject;
    }

    abstract class Modifier {
        abstract T modify(T restoredObject);
    }
}

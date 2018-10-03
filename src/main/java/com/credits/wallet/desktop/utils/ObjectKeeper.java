package com.credits.wallet.desktop.utils;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.credits.wallet.desktop.AppState.account;
import static java.io.File.separator;
import static java.util.concurrent.TimeUnit.*;

public class ObjectKeeper<T> {

    static final Path cacheDirectory = Paths.get(System.getProperty("user.dir") + separator + "cache");
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectKeeper.class);
    private final String fileName;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private volatile T storedObject;

    public ObjectKeeper(String fileName) {
        this.fileName = fileName;
    }

    public void serialize(T object) {
        Lock lock = readWriteLock.writeLock();
        try {
            lock.tryLock(1, SECONDS);
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
            LOGGER.debug("Object was serialized");
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    Path getSerializedObjectPath() {
        return Paths.get(getAccountDirectory() + separator + fileName);
    }

    Path getAccountDirectory() {
        return Paths.get(cacheDirectory + separator + account);
    }

    public T deserialize() {
        if (storedObject == null) {
            Lock lock = readWriteLock.readLock();
            try {
                lock.tryLock(1, SECONDS);
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getSerializedObjectPath().toString()))) {
                    storedObject = (T) ois.readObject();
                }
                LOGGER.debug("Object was deserialized");
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                LOGGER.error(e.getMessage());
                return null;
            } finally {
                lock.unlock();
            }
        }
        return storedObject;
    }
}

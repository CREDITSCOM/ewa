package com.credits.client.node.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static com.credits.general.util.CriticalSection.doSafe;
import static java.io.File.separator;

public class ObjectKeeper<T extends Serializable> {

    static final Path cacheDirectory = Paths.get(System.getProperty("user.dir") + separator + "cache");
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectKeeper.class);
    private final String objectFileName;
    private final String account;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile T storedObject;

    public ObjectKeeper(String account, String objectName) {
        this.account = account;
        this.objectFileName = objectName;
    }

    public void keepObject(T object) {
        if(object != null) {
            doSafe(() -> storedObject = object, lock);
        }
    }

    public void flush(){
        if(storedObject != null) {
            doSafe(() -> serialize(storedObject), lock);
        }
    }

    public Optional<T> getKeptObject() {
        if (storedObject == null) {
            return Optional.ofNullable(storedObject = doSafe(this::deserialize, lock));
        }
        return Optional.ofNullable(storedObject);
    }

    /**
     *  modify deserialized object then serializes it back
     *  <p><b>Attention! you don't have to use ObjectKeeper methods into this method</b></p>
     *
     * @param modifyFunction callback function when take deserialized object or null if object not found
     */
    public void modify(Modifier modifyFunction) {
        doSafe(() -> getKeptObject().ifPresent(oldObject -> keepObject(modifyFunction.modify(oldObject))), lock);
    }

    Path getSerializedObjectPath() {
        return Paths.get(getAccountDirectory() + separator + objectFileName);
    }

    private Path getAccountDirectory() {
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
                LOGGER.error(e.toString());
            return null;
        }
    }

    public abstract class Modifier {
        public abstract T modify(T keptObject);
    }
}

package com.credits.general.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {

    public static Object deserialize(byte[] contractState, ClassLoader classLoader) {
        Object instance;

        try (ObjectInputStream ous = new ObjectInputStreamWithClassLoader(new ByteArrayInputStream(contractState), classLoader)) {
            instance = ous.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Cannot getObject smart contract instance. " + e);
        }
        return instance;
    }

    public static byte[] serialize(Object instance) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream ous = new ObjectOutputStream(baos)) {
            ous.writeObject(instance);
        } catch (IOException e) {
            throw new RuntimeException("Cannot serialize smart contract instance. " + e);
        }
        return baos.toByteArray();
    }
}

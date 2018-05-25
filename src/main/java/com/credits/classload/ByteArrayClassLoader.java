package com.credits.classload;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ByteArrayClassLoader extends ClassLoader {
    private static Map<String, byte[]> classes = new ConcurrentHashMap<>();

    public void loadClass(String address, byte[] byteCode) {
        if (!classes.containsKey(address)) {
            classes.put(address, byteCode);
        }
    }

    public Class<?> buildClass(String address, byte[] byteCode) throws ClassNotFoundException {
        loadClass(address, byteCode);
        byte[] bytes = classes.get(address);
        return defineClass("Contract", bytes, 0, byteCode.length);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] byteCode = classes.get(name);
        if (byteCode == null) {
            throw new ClassNotFoundException("class \"" + name + "\" was not loaded");
        }
        return defineClass(name, byteCode, 0, byteCode.length);
    }
}

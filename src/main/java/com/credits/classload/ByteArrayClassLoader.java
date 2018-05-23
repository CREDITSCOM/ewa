package com.credits.classload;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ByteArrayClassLoader extends ClassLoader {
    private static Map<String, byte[]> classes = new HashMap<>();
    private static ByteArrayClassLoader instance = null;

    private ByteArrayClassLoader() {
        super();
    }

    static public ByteArrayClassLoader getInstance() {
        if (instance == null) {
            return instance = new ByteArrayClassLoader();
        }
        return instance;
    }

    public Class<?> buildClass(String name, byte[] byteCode) throws ClassNotFoundException {
        if (!classes.containsKey(name)) {
            classes.put(name, byteCode);
        }
        byte[] bytes = classes.get(name);
        return defineClass(name, bytes, 0, byteCode.length);
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

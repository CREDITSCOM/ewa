package com.credits.general.util;

import java.util.HashMap;
import java.util.Map;

public class ByteArrayContractClassLoader extends ClassLoader {
    private Map<String, Class<?>> loadedClasses = null;

    public Class<?> buildClass(String className, byte[] byteCode) {
        Class<?> clazz = defineClass(className, byteCode, 0, byteCode.length);
        if(loadedClasses == null) loadedClasses = new HashMap<>();
        loadedClasses.put(className, clazz);
        return clazz;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (loadedClasses != null && loadedClasses.containsKey(name)) {
            return loadedClasses.get(name);
        }
        return super.findClass(name);
    }
}


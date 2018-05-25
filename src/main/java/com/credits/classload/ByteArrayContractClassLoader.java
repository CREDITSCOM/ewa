package com.credits.classload;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ByteArrayContractClassLoader extends ClassLoader {
    private static final String className = "Contract";
    private Class contractClass = null;

    public Class<?> buildClass(byte[] byteCode) {
        return defineClass(className, byteCode, 0, byteCode.length);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.equals(className) && contractClass != null) {
            return contractClass;
        }
        return super.findClass(name);
    }
}

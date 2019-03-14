package com.credits.classload;

import java.util.HashMap;
import java.util.Map;

public class BytecodeContractClassLoader extends ClassLoader {
    private Map<String, byte[]> bytecodes = new HashMap<>();

    public Class<?> loadClass(String className, byte[] byteCode) {
        Class<?> clazz = defineClass(className, byteCode, 0, byteCode.length);
        bytecodes.put(className, byteCode);
        return clazz;
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    public byte[] lookupBytecode(String className) throws ClassNotFoundException {
        if(bytecodes.containsKey(className)){
            return bytecodes.get(className);
        }else {
            throw new ClassNotFoundException(className + " class not loaded");
        }
    }
}

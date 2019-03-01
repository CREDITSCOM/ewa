package com.credits.classload;

public class ByteArrayContractClassLoader extends ClassLoader {
    private static final String className = "MySmartContract";
    private Class contractClass = null;

    public Class<?> buildClass(byte[] byteCode) {
        return defineClass(className, byteCode, 0, byteCode.length);
    }

    public Class<?> buildClass(String className, byte[] byteCode) {
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

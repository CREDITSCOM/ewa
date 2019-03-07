package com.credits.service.contract;

import com.credits.classload.ByteArrayContractClassLoader;

public class  ByteArrayClassLoaderConstructor {
    public static ByteArrayContractClassLoader getByteArrayContractClassLoader() {
        return new ByteArrayContractClassLoader();
    }
}

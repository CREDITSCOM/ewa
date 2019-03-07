package com.credits.service.contract;

import com.credits.classload.ByteArrayContractClassLoader;

public class  ByteArrayClassLoaderConstructor {
    public ByteArrayContractClassLoader getByteArrayContractClassLoader() {
        return new ByteArrayContractClassLoader();
    }
}

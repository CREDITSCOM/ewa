package com.credits.service.usercode;

import com.credits.exception.ClassLoadException;
import com.credits.exception.ContractExecutorException;

import java.io.File;

public interface UserCodeStorageService {

    void store(File file, String address) throws ContractExecutorException;

    default void loadAll() {

    }

    Class<?> load(String address) throws ClassLoadException;

    default void deleteAll() {

    }
}

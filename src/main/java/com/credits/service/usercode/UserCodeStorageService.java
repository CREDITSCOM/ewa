package com.credits.service.usercode;

import com.credits.exception.ClassLoadException;
import com.credits.exception.ContractExecutorException;
import org.springframework.web.multipart.MultipartFile;

public interface UserCodeStorageService {

    void store(MultipartFile file, String address) throws ContractExecutorException;

    default void loadAll() {

    }

    Class<?> load(String address) throws ClassLoadException;

    default void deleteAll() {

    }
}

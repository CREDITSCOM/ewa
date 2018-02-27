package com.credits.service.contract;

import com.credits.exception.ClassLoadException;
import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import com.credits.service.db.leveldb.LevelDbInteractionService;
import com.credits.service.usercode.UserCodeStorageService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

public class ContractExecutionReflectionTest extends ServiceTest {

    @Resource
    private LevelDbInteractionService interactionService;

    @Resource
    private UserCodeStorageService service;

    @Before
    public void setUp() throws ContractExecutorException {
        String fileName = "TestCode1.java";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("com/credits/service/usercode/" + fileName)) {
            MultipartFile file = new MockMultipartFile(fileName, fileName, null, stream);
            service.store(file, "12345");
        } catch (ContractExecutorException | IOException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }
    }

    @Test
    public void executionTest() throws ContractExecutorException {
        Class<?> clazz;
        try {
            clazz = service.load("12345");
        } catch (ClassLoadException e) {
            throw new ContractExecutorException("Cannot execute the contract: " + "12345" + ". Reason: "
                + e.getMessage(), e);
        }

        try {
            Field executorService = clazz.getDeclaredField("service");
            executorService.setAccessible(true);
            executorService.set(null, interactionService);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        Object instance = null;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ContractExecutorException("Cannot execute the contract: " + "12345" + ". Reason: " + e.getMessage(), e);
        }
    }
}

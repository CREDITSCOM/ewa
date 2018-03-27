package com.credits.service.contract;

import com.credits.exception.ClassLoadException;
import com.credits.exception.ContractExecutorException;
import com.credits.serialise.Serializer;
import com.credits.service.ServiceTest;
import com.credits.service.usercode.UserCodeStorageService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

public class ContractExecutorServiceTest extends ServiceTest {

    @Resource
    private ContractExecutorService exService;

    @Resource
    private UserCodeStorageService service;

    private String address = "12345";

    private String SER_SOURCE_FOLDER_PATH = System.getProperty("user.dir") + File.separator + "credits";

    @Before
    public void setUp() throws ContractExecutorException {
        clean(address);

        String fileName = "ContractExecutorServiceTestCode.java";
        File testFile = new File(fileName);
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("com/credits/service/usercode/" + fileName)) {
            FileUtils.copyToFile(stream, testFile);
            service.store(testFile, address);
            testFile.delete();
        } catch (ContractExecutorException | IOException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }
    }

    @Test
    public void executeConstructor() throws ContractExecutorException {
        exService.execute(address, "");

        Class<?> clazz;
        try {
            clazz = service.load(address);
        } catch (ClassLoadException e) {
            throw new ContractExecutorException(
                "Cannot execute the contract: " + address + ". Reason: " + e.getMessage(), e);
        }

        Object instance;
        File serFile = Serializer.getSerFile(address);
        if (serFile.exists()) {
            ClassLoader customLoader = clazz.getClassLoader();
            instance = Serializer.deserialize(serFile, customLoader);
        } else {
            throw new ContractExecutorException("Smart contract instance doesn't exist.");
        }
        int current;
        try {
            Field field = clazz.getDeclaredField("total");
            field.setAccessible(true);
            current = field.getInt(instance);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new ContractExecutorException("Cannot access field.");
        }

        Assert.assertNotNull(instance);
        Assert.assertEquals(1, current);
    }

    @Test
    public void executeMethod() throws ContractExecutorException {
        exService.execute(address, "");
        exService.execute("12345", "foo", null);
        exService.execute("12345", "foo", null);

        Class<?> clazz;
        try {
            clazz = service.load(address);
        } catch (ClassLoadException e) {
            throw new ContractExecutorException(
                "Cannot execute the contract: " + address + ". Reason: " + e.getMessage(), e);
        }

        Object instance;
        File serFile = Serializer.getSerFile(address);
        if (serFile.exists()) {
            ClassLoader customLoader = clazz.getClassLoader();
            instance = Serializer.deserialize(serFile, customLoader);
        } else {
            throw new ContractExecutorException("Smart contract instance doesn't exist.");
        }
        int current;
        try {
            Field field = clazz.getDeclaredField("total");
            field.setAccessible(true);
            current = field.getInt(instance);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new ContractExecutorException("Cannot access field.");
        }

        Assert.assertNotNull(instance);
        Assert.assertEquals(21, current);

        File sourcePath = new File(SER_SOURCE_FOLDER_PATH + File.separator + address);
        String fileName = sourcePath.listFiles()[0].getName();
        String serFileName = FilenameUtils.getBaseName(fileName) + ".out";
        new File(SER_SOURCE_FOLDER_PATH + File.separator + address + File.separator + serFileName).delete();
    }
}

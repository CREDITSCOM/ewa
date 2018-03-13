package com.credits.service.contract;

import com.credits.exception.ClassLoadException;
import com.credits.exception.ContractExecutorException;
import com.credits.serialise.Serializer;
import com.credits.service.ServiceTest;
import com.credits.service.usercode.UserCodeStorageService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

public class ContractExecutorServiceMethodsInvokingTest extends ServiceTest {

    @Resource
    private ContractExecutorService service;

    @Resource
    private UserCodeStorageService storageService;

    private final String address = "1a2b";

    @Before
    public void setUp() throws ContractExecutorException {
        clear(address);

        String fileName = "MethodsInvokingTestCode.java";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("com/credits/service/usercode/" + fileName)) {
            MultipartFile file = new MockMultipartFile(fileName, fileName, null, stream);
            storageService.store(file, address);
        } catch (ContractExecutorException | IOException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }

        service.execute(address);
    }

    @Test
    public void primitiveExecutionTest() throws ContractExecutorException {
        String[] params = {"\"test string\"", "200", "3f"};
        service.execute(address, "foo", params);
    }

    @Test
    public void objectExecutionTest() throws ContractExecutorException {
        String[] params = {"\"test string\"", "200d", "3"};
        service.execute(address, "foo", params);
    }

    @Test
    public void arrayExecutionTest() throws ContractExecutorException {
        String[] params = { "{\"test1\", \"test2\", \"test3\"}" };
        service.execute(address, "main", params);

        params = new String[]{"{1, 2, 3}"};
        service.execute(address, "main", params);

        params = new String[]{"{1d, 2d, 3d}"};
        service.execute(address, "main", params);
    }

    @Test
    public void arrayBooleanTest() throws ContractExecutorException {
        String[] params = {"{false, true}"};
        service.execute(address, "foo", params);
    }

    @Test
    public void arrayIntTest() throws ContractExecutorException {
        String[] params = {"{1, 2}"};
        service.execute(address, "foo", params);
    }

    @Test
    public void arrayShortTest() throws ContractExecutorException {
        String[] params = {"{(short)1, (short)2}"};
        service.execute(address, "foo", params);
    }

    @Test
    public void arrayLongTest() throws ContractExecutorException {
        String[] params = {"{1l, 2l}"};
        service.execute(address, "foo", params);
    }

    @Test
    public void arrayFloatTest() throws ContractExecutorException {
        String[] params = {"{1f, .2f}"};
        service.execute(address, "foo", params);
    }

    @Test
    public void globalVarInstanceTest() throws ContractExecutorException {
        service.execute(address, "globalVarInstance", new String[0]);
        service.execute(address, "globalVarInstance", new String[0]);

        Class<?> clazz;
        try {
            clazz = storageService.load(address);
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
            Field field = clazz.getDeclaredField("intVar");
            field.setAccessible(true);
            current = field.getInt(instance);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new ContractExecutorException("Cannot access field.");
        }

        Assert.assertNotNull(instance);
        Assert.assertEquals(3, current);
    }
}

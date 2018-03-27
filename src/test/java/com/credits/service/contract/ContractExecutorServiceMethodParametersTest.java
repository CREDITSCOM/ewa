package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import com.credits.service.usercode.UserCodeStorageService;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ContractExecutorServiceMethodParametersTest extends ServiceTest {

    @Resource
    private ContractExecutorService service;

    @Resource
    private UserCodeStorageService storageService;

    private final String address = "1a2b";

    @Before
    public void setUp() throws ContractExecutorException {
        clean(address);

        String fileName = "ContractExecutorServiceMethodParametersTestCode.java";
        File testFile = new File(fileName);
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("com/credits/service/usercode/" + fileName)) {
            FileUtils.copyToFile(stream, testFile);
            storageService.store(testFile, address);
            testFile.delete();
        } catch (ContractExecutorException | IOException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }

        service.execute(address, "");
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
}

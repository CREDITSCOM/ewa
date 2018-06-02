package com.credits.service.usercode;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class UserCodeStorageServiceTest extends ServiceTest {

    @Resource
    private UserCodeStorageService service;

    private final String address = "123456abcde";

    @Before
    public void setUp() {
    }

    @Test
    public void storeTest() throws ContractExecutorException {
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
}

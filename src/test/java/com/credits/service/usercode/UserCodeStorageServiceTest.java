package com.credits.service.usercode;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static java.io.File.separator;

public class UserCodeStorageServiceTest extends ServiceTest {

    @Resource
    private UserCodeStorageService service;

    private final String address = "123456abcde";

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileSystemUtils.deleteRecursively(new File(dir));
    }

    @Ignore
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

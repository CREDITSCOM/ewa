package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ContractExecutorServiceTest extends ServiceTest {

    @Resource
    private ContractExecutorService service;

    private final String address = "1a2b";


    @Before
    public void setUp() throws ContractExecutorException {
        final String destFolder = System.getProperty("user.dir") + File.separator + "credits";
        URL resource = getClass().getClassLoader().getResource("com/credits/service/contract/UserCodeTest.class");
        Assert.assertNotNull(resource);

        File source = new File(resource.getFile());

        String destFilePath = destFolder + File.separator + address + File.separator + source.getName();
        File dest = new File(destFilePath);
        dest.getParentFile().mkdirs();

        try {
            FileUtils.copyFile(source, dest);
        } catch (IOException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }
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
        String[] params = {"\"test1\"", "\"test2\"", "\"test3\""};
        service.execute(address, "main", params);
    }
}

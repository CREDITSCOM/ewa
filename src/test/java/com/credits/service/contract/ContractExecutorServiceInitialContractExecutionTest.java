package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import com.credits.service.usercode.UserCodeStorageService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ContractExecutorServiceInitialContractExecutionTest extends ServiceTest {

    @Resource
    private ContractExecutorService exService;

    @Resource
    private UserCodeStorageService service;

    private String address = "12345";

    @Before
    public void setUp() throws ContractExecutorException {
        clear(address);

        String fileName = "InitialContractExecutionTestCode.java";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("com/credits/service/usercode/" + fileName)) {
            MultipartFile file = new MockMultipartFile(fileName, fileName, null, stream);
            service.store(file, address);
        } catch (ContractExecutorException | IOException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }
    }

    @Test
    public void executionTest() throws ContractExecutorException {
        exService.execute("12345");
    }
}

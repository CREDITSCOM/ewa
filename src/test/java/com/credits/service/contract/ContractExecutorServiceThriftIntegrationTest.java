package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import com.credits.service.usercode.UserCodeStorageService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

public class ContractExecutorServiceThriftIntegrationTest extends ServiceTest{
    @Resource
    private ContractExecutorService ceService;

    @Resource
    private UserCodeStorageService userCodeService;

    private final String address = "1a2b3c";

    @Before
    public void setUp() throws ContractExecutorException {
        clean(address);

        String fileName = "ContractExecutorServiceThriftIntegrationTestCode.java";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("com/credits/service/usercode/" + fileName)) {
            MultipartFile file = new MockMultipartFile(fileName, fileName, null, stream);
            userCodeService.store(file, address);
        } catch (ContractExecutorException | IOException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }

        ceService.execute(address);
    }

    @Test
    public void executionTest() throws ContractExecutorException {
        ceService.execute(address, "foo", null);
    }
}

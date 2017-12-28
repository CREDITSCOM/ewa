package com.credits.service.usercode;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

public class UserCodeStorageServiceTest extends ServiceTest {

    @Resource
    private UserCodeStorageService service;

    @Test
    public void storeTest() throws ContractExecutorException {
        String fileName = "UserCodeTest.java";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("com/credits/service/usercode/" + fileName)) {
            MultipartFile file = new MockMultipartFile(fileName, fileName, null, stream);
            service.store(file, "123456abcde");
        } catch (ContractExecutorException | IOException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }
    }
}

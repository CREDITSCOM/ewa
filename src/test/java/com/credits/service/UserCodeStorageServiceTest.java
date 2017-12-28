package com.credits.service;

import com.credits.exception.ContractExecutorException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserCodeStorageServiceTest {

    @Resource
    StorageService service;

    @Test
    public void storeTest() throws ContractExecutorException {
        try (FileInputStream stream = new FileInputStream("src/test/resources/com/credits/compilation/Test.java")) {
            MultipartFile file = new MockMultipartFile("unitTestFile.java", stream);
            service.store(file, "123456abcde");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

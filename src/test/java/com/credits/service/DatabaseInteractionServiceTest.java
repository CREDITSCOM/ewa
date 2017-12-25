package com.credits.service;

import com.credits.exception.ContractExecutorException;
import com.credits.vo.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseInteractionServiceTest {

    private DatabaseInteractionService service;

    @Before
    public void setUp() {
        Properties properties = new Properties();
        try (FileInputStream is = new FileInputStream(new File("src/test/resources/application.properties"))){
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url = properties.getProperty("leveldb.url");
        service = new DatabaseInteractionService(new RestTemplate(), url);
    }

//    @Test
//    public void postTest() throws ContractExecutorException {
//        service.post(new Transaction("124", 126, '+'));
//    }

    @Test
    public void getTest() {
        service.get("124");
    }

}

package com.credits.service;

import com.credits.exception.ContractExecutorException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ContractExecutorServiceTest {

    @Resource
    private ContractExecutorService service;

    @Test
    public void executionTest() throws ContractExecutorException {
        String[] params = {"\"test string\"", "(short) 200", "3f"};

        service.execute("1a2b", "foo", params);
    }
}

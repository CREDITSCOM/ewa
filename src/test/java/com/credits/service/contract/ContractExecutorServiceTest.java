package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import org.junit.Test;

import javax.annotation.Resource;

public class ContractExecutorServiceTest extends ServiceTest {

    @Resource
    private ContractExecutorService service;

    @Test
    public void executionTest() throws ContractExecutorException {
        //TODO: add manually here in this method a java file to the right path otherwise this test is not going to work out

        String[] params = {"\"test string\"", "(short) 200", "3f"};

        service.execute("1a2b", "foo", params);
    }
}

package com.credits.service;

import com.credits.exception.ContractExecutorException;
import com.credits.vo.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DatabaseInteractionServiceTest {

    @Resource
    private DatabaseInteractionService service;

//    @Test
//    public void postTest() throws ContractExecutorException {
//        service.post(new Transaction("1111", 129, '+'));
//    }

    @Test
    public void getTest() throws ContractExecutorException {
        String idActual = "333";
        service.post(new Transaction(idActual, 129, '+'));
        Transaction[] transactions = service.get(idActual, 0);
        Assert.assertNotNull(transactions);
        Assert.assertNotEquals(0, transactions.length);
        Transaction transaction = transactions[0];
        Assert.assertNotNull(transaction);
        Assert.assertEquals(idActual, transaction.getId());

    }

}

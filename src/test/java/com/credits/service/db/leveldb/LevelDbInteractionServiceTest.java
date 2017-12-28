package com.credits.service.db.leveldb;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import com.credits.vo.usercode.Transaction;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

public class LevelDbInteractionServiceTest extends ServiceTest {

    @Resource
    private LevelDbInteractionService service;

    @Test
    public void postTest() throws ContractExecutorException {
        service.put(new Transaction("1111", 129, '+'));
    }

    @Test
    public void getTest() throws ContractExecutorException {
        String idActual = "333";
        service.put(new Transaction(idActual, 129, '+'));
        Transaction[] transactions = service.get(idActual, 0);
        Assert.assertNotNull(transactions);
        Assert.assertNotEquals(0, transactions.length);
        Transaction transaction = transactions[0];
        Assert.assertNotNull(transaction);
        Assert.assertEquals(idActual, transaction.getId());

    }

}

package com.credits.service.db.leveldb;

import com.credits.leveldb.client.PoolData;
import com.credits.leveldb.client.TransactionData;
import com.credits.service.ServiceTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

public class LevelDbInteractionServiceTest extends ServiceTest{

    @Resource
    private LevelDbInteractionService service;

    @Test
    public void perform() throws Exception {
        System.out.println("getBalance()");
        Double balance = service.getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", "CS");
        System.out.println("getBalance=" + balance);

        System.out.println("getTransaction()");
        TransactionData transaction = service.getTransaction("00000000000001adf44c7d697675870");
        System.out.println("getTransaction=" + transaction);

        System.out.println("getTransactions()");
        List<TransactionData> transactions = service.getTransactions("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", 0, 5);
        System.out.println("getTransactions=" + transactions);

        System.out.println("getPool()");
        PoolData poolData = service.getPool(1);
        System.out.println("getPool=" + poolData);
    }

}

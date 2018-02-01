package com.credits.service.db.leveldb;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import com.credits.thrift.gen.api.BalanceGetResult;
import com.credits.thrift.gen.api.TransactionGetResult;
import com.credits.thrift.gen.api.TransactionsGetResult;
import org.apache.thrift.TException;
import org.junit.Test;

import javax.annotation.Resource;

public class LevelDbInteractionServiceTest extends ServiceTest{

    @Resource
    private LevelDbInteractionService service;

    @Test
    public void perform() throws TException, ContractExecutorException {
        System.out.println("getBalance()");
        BalanceGetResult balance = service.getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", "CS");
        System.out.println("getBalance=" + balance.getAmount());

        System.out.println("getTransaction()");
        TransactionGetResult transaction = service.getTransaction("00000000000001adf44c7d697675870");
        System.out.println("getTransaction=" + transaction.getTransaction());

        System.out.println("getTransactions()");
        TransactionsGetResult transactions = service.getTransactions("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", 1, 5);
        System.out.println("getTransactions=" + transactions.getTransactions());
    }

}

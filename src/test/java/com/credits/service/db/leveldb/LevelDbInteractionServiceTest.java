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
        Double balance = service.getBalance("1_GJnW172n4CdN0", "cs");
        System.out.println("getBalance=" + balance);

        System.out.println("getTransaction()");
        TransactionData transaction = service.getTransaction("43dd3e46-cf73-2d43-a1c5-546061a4c900");
        System.out.println("getTransaction=" + transaction);

        System.out.println("getTransactions()");
        List<TransactionData> transactions = service.getTransactions("1_GJnW172n4CdN0", 0, 20);
        System.out.println("getTransactions=" + transactions);

        service.transactionFlow("", "", "", "", 2d, "");

//        System.out.println("getPool()");
//        PoolData poolData = service.getPool(0);
//        System.out.println("getPool: poolNumber= " + poolData.getPoolNumber() + " transactionsCount= "
//            + poolData.getTransactionsCount() + " hash= " + poolData.getPoolHash() + " prevHash= "
//            + poolData.getPrevPoolHash() + " time= " + poolData.getCreationTime());
//
//        System.out.println("getPoolList()");
//        List<PoolData> poolList = service.getPoolList(0, 5);
//        System.out.println("getPoolList: ");
//        for (PoolData poolData1 : poolList) {
//            System.out.println("poolNumber= " + poolData1.getPoolNumber() + " transactionsCount= "
//                + poolData1.getTransactionsCount() + " hash= " + poolData1.getPoolHash() + " prevHash= "
//                + poolData1.getPrevPoolHash() + " time= " + poolData1.getCreationTime());
//        }
    }

}

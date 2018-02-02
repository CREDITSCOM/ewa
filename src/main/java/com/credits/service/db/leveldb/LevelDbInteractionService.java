package com.credits.service.db.leveldb;

import com.credits.leveldb.client.PoolData;
import com.credits.leveldb.client.TransactionData;

import java.util.List;

public interface LevelDbInteractionService {

    Double getBalance(String address, String currency) throws Exception;

    TransactionData getTransaction(String transactionId) throws Exception;

    List<TransactionData> getTransactions(String address, long offset, long limit) throws Exception;

    List<PoolData> getPoolList(long offset, long limit) throws Exception;

    PoolData getPool(long poolNumber) throws Exception;

}

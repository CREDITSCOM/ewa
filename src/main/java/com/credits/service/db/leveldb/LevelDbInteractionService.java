package com.credits.service.db.leveldb;

import com.credits.leveldb.client.PoolData;
import com.credits.leveldb.client.TransactionData;

import java.util.List;

public interface LevelDbInteractionService {

    Double getBalance(String address, String currency) throws Exception;

    TransactionData getTransaction(String transactionId) throws Exception;

    List<TransactionData> getTransactions(String address, long offset, long limit) throws Exception;

    List<PoolData> getPoolList(long offset, long limit) throws Exception;

    PoolData getPool(String poolNumber) throws Exception;

    void transactionFlow(String hash, String innerId, String source, String target, Double amount, String currency, String signatureBASE64) throws Exception;

    String getHash();

    String getInnerId();

}

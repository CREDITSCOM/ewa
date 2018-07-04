package com.credits.service.db.leveldb;

import com.credits.leveldb.client.data.PoolData;
import com.credits.leveldb.client.data.TransactionData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;

import java.math.BigDecimal;
import java.util.List;

public interface LevelDbInteractionService {

    BigDecimal getBalance(String address, String currency) throws LevelDbClientException, CreditsNodeException;

    TransactionData getTransaction(String transactionId) throws LevelDbClientException, CreditsNodeException;

    List<TransactionData> getTransactions(String address, long offset, long limit) throws LevelDbClientException, CreditsNodeException;

    List<PoolData> getPoolList(long offset, long limit) throws LevelDbClientException, CreditsNodeException;

    PoolData getPoolInfo(byte[] hash, long index) throws LevelDbClientException, CreditsNodeException;

    void transactionFlow(String innerId,
                         String source,
                         String target,
                         BigDecimal amount,
                         BigDecimal balance,
                         String currency,
                         String signature) throws LevelDbClientException, CreditsNodeException;
}

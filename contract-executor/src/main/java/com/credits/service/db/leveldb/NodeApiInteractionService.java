package com.credits.service.db.leveldb;


import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.PoolData;
import com.credits.client.node.pojo.TransactionData;
import com.credits.general.util.exception.ConverterException;

import java.math.BigDecimal;
import java.util.List;

public interface NodeApiInteractionService {

    BigDecimal getBalance(String address, byte currency) throws NodeClientException, ConverterException;

    TransactionData getTransaction(String transactionId) throws NodeClientException;

    List<TransactionData> getTransactions(String address, long offset, long limit) throws NodeClientException, ConverterException;

    List<PoolData> getPoolList(long offset, long limit) throws NodeClientException;

    PoolData getPoolInfo(byte[] hash, long index) throws NodeClientException;

    void transactionFlow(Long innerId,
                         String source,
                         String target,
                         BigDecimal amount,
                         BigDecimal balance,
                         byte currency,
                         byte[] signature,
                         BigDecimal fee) throws ConverterException, NodeClientException;
}

package com.credits.service.node.api;


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

    void transactionFlow(String source, String target, double amount, double fee, byte[] userData,
        String specialProperty) throws ConverterException, NodeClientException;
}

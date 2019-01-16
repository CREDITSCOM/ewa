package com.credits.client.node.service;


import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.*;
import com.credits.general.util.exception.ConverterException;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;

public interface NodeApiService {

    BigDecimal getBalance(String address) throws NodeClientException, ConverterException;

    List<TransactionData> getTransactions(String address, long offset, long limit) throws NodeClientException, ConverterException;

    @Deprecated
    TransactionData getTransaction(TransactionIdData transactionIdData) throws NodeClientException;

    PoolData getPoolInfo(byte[] hash, long index) throws NodeClientException;

    List<PoolData> getPoolList(Long offset, Long limit) throws NodeClientException;

    TransactionFlowResultData smartContractTransactionFlow(SmartContractTransactionFlowData scData) throws NodeClientException, ConverterException;

    TransactionFlowResultData transactionFlow(TransactionFlowData transaction);

    SmartContractData getSmartContract(String address) throws NodeClientException, ConverterException;

    List<SmartContractData> getSmartContracts(String address) throws NodeClientException, ConverterException;

    List<ByteBuffer> getSmartContractAddresses(String address) throws NodeClientException, ConverterException;

    WalletData getWalletData(String address) throws NodeClientException, ConverterException;

    Integer getWalletId(String address) throws NodeClientException, ConverterException;

    Long getWalletTransactionsCount(String address) throws NodeClientException, ConverterException;

    TransactionsStateGetResultData getTransactionsState(String address, List<Long> transactionIdList) throws NodeClientException, ConverterException;
}

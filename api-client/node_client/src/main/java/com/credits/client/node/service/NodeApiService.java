package com.credits.client.node.service;


import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.PoolData;
import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.pojo.SmartContractTransactionData;
import com.credits.client.node.pojo.SmartContractTransactionFlowData;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.client.node.pojo.TransactionFlowResultData;
import com.credits.client.node.pojo.TransactionIdData;
import com.credits.client.node.pojo.TransactionsStateGetResultData;
import com.credits.client.node.pojo.WalletData;
import com.credits.general.util.exception.ConverterException;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;

public interface NodeApiService {

    BigDecimal getBalance(String address) throws NodeClientException, ConverterException;

    Pair<Integer, Long> getBlockAndSynchronizePercent() throws NodeClientException, ConverterException;

    List<TransactionData> getTransactions(String address, long offset, long limit) throws NodeClientException, ConverterException;

    List<SmartContractTransactionData> getSmartContractTransactions(String address, long offset, long limit)
            throws NodeClientException, ConverterException;

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

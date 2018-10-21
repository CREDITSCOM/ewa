package com.credits.client.node.service;


import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.CreateTransactionData;
import com.credits.client.node.pojo.PoolData;
import com.credits.client.node.pojo.SmartContractInvocationData;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.pojo.TransactionIdData;
import com.credits.client.node.pojo.WalletData;
import com.credits.client.node.thrift.TransactionsStateGetResult;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.util.exception.ConverterException;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;

public interface NodeApiService {

    BigDecimal getBalance(String address) throws NodeClientException, ConverterException;

    List<TransactionData> getTransactions(String address, long offset, long limit) throws NodeClientException, ConverterException;

    TransactionData getTransaction(TransactionIdData transactionIdData) throws NodeClientException;

    PoolData getPoolInfo(byte[] hash, long index) throws NodeClientException;

    List<PoolData> getPoolList(Long offset, Long limit) throws NodeClientException;

    void executeSmartContract(long transactionInnerId, String source, String target, SmartContractInvocationData smartContractInvocationData, byte[] signature,
        TransactionProcessThread.Callback callback) throws NodeClientException, ConverterException;

    ApiResponseData createTransaction(CreateTransactionData createTransactionData, boolean checkBalance) throws NodeClientException, ConverterException;

    void asyncCreateTransaction(CreateTransactionData createTransactionData, boolean checkBalance, TransactionProcessThread.Callback callback)
        throws NodeClientException, ConverterException;

    SmartContractData getSmartContract(String address) throws NodeClientException, ConverterException;

    List<SmartContractData> getSmartContracts(String address) throws NodeClientException, ConverterException;

    List<ByteBuffer> getSmartContractAddresses(String address) throws NodeClientException, ConverterException;

    WalletData getWalletData(String address) throws NodeClientException, ConverterException;

    Integer getWalletId(String address) throws NodeClientException, ConverterException;

    Long getWalletTransactionsCount(String address) throws NodeClientException, ConverterException;

    TransactionsStateGetResult getTransactionsState(String address, List<Long> transactionIdList) throws NodeClientException, ConverterException;
}

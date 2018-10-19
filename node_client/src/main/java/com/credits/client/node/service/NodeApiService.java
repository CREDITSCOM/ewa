package com.credits.client.node.service;


import com.credits.client.node.exception.CreditsNodeException;
import com.credits.client.node.exception.NodeClientException;
import com.credits.general.pojo.ApiResponseData;
import com.credits.client.node.pojo.CreateTransactionData;
import com.credits.client.node.pojo.PoolData;
import com.credits.general.pojo.SmartContractData;
import com.credits.client.node.pojo.SmartContractInvocationData;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.pojo.TransactionIdData;
import com.credits.client.node.pojo.WalletData;
import com.credits.client.node.thrift.TransactionsStateGetResult;
import com.credits.general.exception.CreditsGeneralException;
import com.credits.general.exception.GeneralClientException;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;

public interface NodeApiService {

    BigDecimal getBalance(String address) throws NodeClientException, CreditsNodeException, CreditsGeneralException;

    List<TransactionData> getTransactions(String address, long offset, long limit)
        throws NodeClientException, CreditsNodeException, CreditsGeneralException;

    TransactionData getTransaction(TransactionIdData transactionIdData)
            throws NodeClientException, CreditsNodeException;

    PoolData getPoolInfo(byte[] hash, long index) throws NodeClientException, CreditsNodeException;

    List<PoolData> getPoolList(Long offset, Long limit) throws NodeClientException, CreditsNodeException;

    void executeSmartContract(long transactionInnerId, String source, String target, SmartContractInvocationData smartContractInvocationData, byte[] signature,
        TransactionProcessThread.Callback callback)
        throws NodeClientException, CreditsNodeException, CreditsGeneralException;

    ApiResponseData createTransaction(CreateTransactionData createTransactionData, boolean checkBalance)
        throws NodeClientException, CreditsNodeException, CreditsGeneralException, GeneralClientException;

    void asyncCreateTransaction(CreateTransactionData createTransactionData, boolean checkBalance, TransactionProcessThread.Callback callback)
        throws NodeClientException, CreditsNodeException, CreditsGeneralException, GeneralClientException;

    SmartContractData getSmartContract(String address)
        throws NodeClientException, CreditsNodeException, CreditsGeneralException;

    List<SmartContractData> getSmartContracts(String address)
        throws NodeClientException, CreditsNodeException, CreditsGeneralException;

    List<ByteBuffer> getSmartContractAddresses(String address)
        throws NodeClientException, CreditsNodeException, CreditsGeneralException;

    WalletData getWalletData(String address) throws NodeClientException, CreditsNodeException, CreditsGeneralException;

    Integer getWalletId(String address) throws NodeClientException, CreditsNodeException, CreditsGeneralException;

    Long getWalletTransactionsCount(String address)
        throws NodeClientException, CreditsNodeException, CreditsGeneralException;

    TransactionsStateGetResult getTransactionsState(String address, List<Long> transactionIdList)
        throws CreditsNodeException, NodeClientException, CreditsGeneralException;
}

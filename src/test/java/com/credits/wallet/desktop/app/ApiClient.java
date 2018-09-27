package com.credits.wallet.desktop.app;

import com.credits.common.utils.Converter;
import com.credits.leveldb.client.ApiClientInterface;
import com.credits.leveldb.client.data.ApiResponseData;
import com.credits.leveldb.client.data.CreateTransactionData;
import com.credits.leveldb.client.data.PoolData;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.leveldb.client.data.SmartContractInvocationData;
import com.credits.leveldb.client.data.TransactionData;
import com.credits.leveldb.client.data.TransactionIdData;
import com.credits.leveldb.client.data.WalletData;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.thrift.Transaction;
import com.credits.leveldb.client.util.TransactionType;
import com.credits.wallet.desktop.utils.FormUtils;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;

public class ApiClient implements ApiClientInterface {


    public BigDecimal getBalance(byte[] address) {
        String source = Converter.encodeToBASE58(address);
        if (source.equals("GWe8WZYLBxAqsfPZgejnysXQm5Q697VSsyr3x59RvYBf")) {
            return BigDecimal.valueOf(100000.99);
        }
        return null;
    }

    public List<TransactionData> getTransactions(byte[] address, long offset, long limit) {
        return FakeData.getTransactionDataList();
    }

    public TransactionData getTransaction(TransactionIdData transactionIdData) {
        return null;
    }

    public PoolData getPoolInfo(byte[] hash, long index) {
        return null;
    }

    public List<PoolData> getPoolList(Long offset, Long limit) {
        return null;
    }

    private ApiResponseData asyncTransactionFlowInternal(Transaction transaction, TransactionType transactionType)
        throws LevelDbClientException {
        return null;
    }

    public ApiResponseData deploySmartContract(long transactionInnerId, byte[] transactionSource,
        byte[] transactionTarget, SmartContractInvocationData smartContractInvocationData, byte[] signature,
        TransactionType transactionType) {
        return null;
    }

    public ApiResponseData executeSmartContract(long transactionInnerId, byte[] transactionSource,
        byte[] transactionTarget, SmartContractInvocationData smartContractInvocationData, byte[] signature,
        TransactionType transactionType) {
        FormUtils.showInfo(smartContractInvocationData.getMethod());
        if(smartContractInvocationData.getMethod().equals("getName")) {
            return null;
        }
        if(smartContractInvocationData.getMethod().equals("getBalance")) {
            return null;
        }

        return null;
    }

    public ApiResponseData createTransaction(CreateTransactionData createTransactionData, boolean checkBalance) {
        return null;
    }

    private ApiResponseData syncTransactionFlowInternal(Transaction transaction) throws LevelDbClientException {
        return null;
    }

    private Transaction createTransactionProcess(CreateTransactionData createTransactionData, boolean checkBalance) {
        return null;
    }

    public ApiResponseData asyncCreateTransaction(CreateTransactionData createTransactionData, boolean checkBalance,
        TransactionType transactionType) {
        return null;
    }

    public SmartContractData getSmartContract(byte[] address) {
        return null;
    }

    public List<SmartContractData> getSmartContracts(byte[] address) {
        return FakeData.getSmartContractData();
    }

    public List<ByteBuffer> getSmartContractAddresses(byte[] address) {
        return null;
    }

    public WalletData getWalletData(byte[] address) {
        return null;
    }


    public Integer getWalletId(byte[] address) {
        String source = Converter.encodeToBASE58(address);
        if(source.equals("GWe8WZYLBxAqsfPZgejnysXQm5Q697VSsyr3x59RvYBf"))
            return 1;
        if(source.equals("11111111111111111111111111111111111111111111"))
            return 0;
        if(source.equals("22222222222222222222222222222222222222222222"))
            return 3;
        if(source.equals("33333333333333333333333333333333333333333333"))
            return 4;
        return null;
    }

    public Long getWalletTransactionsCount(byte[] address) {
        return 100L;
    }
}
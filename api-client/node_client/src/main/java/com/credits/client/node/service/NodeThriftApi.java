package com.credits.client.node.service;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.TransactionIdData;
import com.credits.client.node.thrift.generated.PoolInfoGetResult;
import com.credits.client.node.thrift.generated.PoolListGetResult;
import com.credits.client.node.thrift.generated.SmartContractAddressesListGetResult;
import com.credits.client.node.thrift.generated.SmartContractGetResult;
import com.credits.client.node.thrift.generated.SmartContractsListGetResult;
import com.credits.client.node.thrift.generated.TransactionGetResult;
import com.credits.client.node.thrift.generated.TransactionsGetResult;
import com.credits.client.node.thrift.generated.TransactionsStateGetResult;
import com.credits.client.node.thrift.generated.WalletBalanceGetResult;
import com.credits.client.node.thrift.generated.WalletDataGetResult;
import com.credits.client.node.thrift.generated.WalletIdGetResult;
import com.credits.client.node.thrift.generated.WalletTransactionsCountGetResult;

import java.nio.ByteBuffer;
import java.util.List;

interface NodeThriftApi {
    TransactionsGetResult getTransactions(byte[] address, long offset, long limit) throws NodeClientException;

    TransactionGetResult getTransaction(TransactionIdData transactionIdData) throws NodeClientException;

    PoolInfoGetResult getPoolInfo(ByteBuffer hashByteBuffer, long index) throws NodeClientException;

    PoolListGetResult getPoolList(Long offset, Long limit) throws NodeClientException;

    WalletBalanceGetResult getBalance(byte[] address) throws NodeClientException;

    SmartContractGetResult getSmartContract(byte[] address) throws NodeClientException;

    SmartContractsListGetResult getSmartContracts(byte[] address) throws NodeClientException;

    SmartContractAddressesListGetResult getSmartContractAddresses(byte[] address) throws NodeClientException;

    WalletDataGetResult getWalletData(byte[] address) throws NodeClientException;

    WalletIdGetResult getWalletId(byte[] address) throws NodeClientException;

    WalletTransactionsCountGetResult getWalletTransactionsCount(byte[] address) throws NodeClientException;

    TransactionsStateGetResult getTransactionsState(byte[] address, List<Long> transactionIdList) throws NodeClientException;

}

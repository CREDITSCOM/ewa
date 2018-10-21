package com.credits.client.node.service;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.TransactionIdData;
import com.credits.client.node.thrift.PoolInfoGetResult;
import com.credits.client.node.thrift.PoolListGetResult;
import com.credits.client.node.thrift.SmartContractAddressesListGetResult;
import com.credits.client.node.thrift.SmartContractGetResult;
import com.credits.client.node.thrift.SmartContractsListGetResult;
import com.credits.client.node.thrift.TransactionGetResult;
import com.credits.client.node.thrift.TransactionsGetResult;
import com.credits.client.node.thrift.TransactionsStateGetResult;
import com.credits.client.node.thrift.WalletBalanceGetResult;
import com.credits.client.node.thrift.WalletDataGetResult;
import com.credits.client.node.thrift.WalletIdGetResult;
import com.credits.client.node.thrift.WalletTransactionsCountGetResult;

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

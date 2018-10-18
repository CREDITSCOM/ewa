package com.credits.client.node.service;

import com.credits.client.node.exception.CreditsNodeException;
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
    TransactionsGetResult getTransactions(byte[] address, long offset, long limit) throws NodeClientException, CreditsNodeException;

    TransactionGetResult getTransaction(TransactionIdData transactionIdData) throws NodeClientException, CreditsNodeException;

    PoolInfoGetResult getPoolInfo(ByteBuffer hashByteBuffer, long index) throws NodeClientException;

    PoolListGetResult getPoolList(Long offset, Long limit) throws NodeClientException, CreditsNodeException;

    WalletBalanceGetResult getBalance(byte[] address) throws NodeClientException, CreditsNodeException;

    SmartContractGetResult getSmartContract(byte[] address) throws NodeClientException, CreditsNodeException;

    SmartContractsListGetResult getSmartContracts(byte[] address) throws NodeClientException, CreditsNodeException;

    SmartContractAddressesListGetResult getSmartContractAddresses(byte[] address) throws NodeClientException, CreditsNodeException;

    WalletDataGetResult getWalletData(byte[] address) throws NodeClientException, CreditsNodeException;

    WalletIdGetResult getWalletId(byte[] address) throws NodeClientException, CreditsNodeException;

    WalletTransactionsCountGetResult getWalletTransactionsCount(byte[] address) throws NodeClientException, CreditsNodeException;

    TransactionsStateGetResult getTransactionsState(byte[] address, List<Long> transactionIdList) throws NodeClientException;

}

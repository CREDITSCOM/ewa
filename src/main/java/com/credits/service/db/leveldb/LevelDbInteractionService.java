package com.credits.service.db.leveldb;

import com.credits.exception.ContractExecutorException;
import com.credits.thrift.gen.api.BalanceGetResult;
import com.credits.thrift.gen.api.PoolGetResult;
import com.credits.thrift.gen.api.PoolListGetResult;
import com.credits.thrift.gen.api.TransactionGetResult;
import com.credits.thrift.gen.api.TransactionsGetResult;

public interface LevelDbInteractionService {

    BalanceGetResult getBalance(String address, String currency) throws ContractExecutorException;

    TransactionGetResult getTransaction(String transactionId) throws ContractExecutorException;

    TransactionsGetResult getTransactions(String address, long offset, long limit) throws ContractExecutorException;

    PoolListGetResult getPoolList(long offset, long limit) throws ContractExecutorException;

    PoolGetResult getPool(long poolNumber) throws ContractExecutorException;

}

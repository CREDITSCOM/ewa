package com.credits.service.db.leveldb;

import com.credits.exception.ContractExecutorException;
import com.credits.thrift.gen.api.Amount;
import com.credits.thrift.gen.api.TransactionInfo;
import com.credits.vo.usercode.Transaction;

import java.util.List;
import java.util.Map;

public interface LevelDbInteractionService {
    Transaction[] get(String id, int value);

    void put(Transaction transaction) throws ContractExecutorException;

    Map<String, Amount> getBalance(String address);

    List<com.credits.thrift.gen.api.Transaction> getTransactions(String address, String currency);

    TransactionInfo getTransactionInfo(String source, String destination, Amount amount, long timestamp, String currency);
}

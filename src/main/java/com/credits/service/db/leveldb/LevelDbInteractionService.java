package com.credits.service.db.leveldb;

import com.credits.exception.ContractExecutorException;
import com.credits.vo.usercode.Transaction;

public interface LevelDbInteractionService {
    Transaction[] get(String id, int value);

    void put(Transaction transaction) throws ContractExecutorException;
}

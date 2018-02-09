package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;

public interface ContractExecutorService {
    void execute(String address) throws ContractExecutorException;
}

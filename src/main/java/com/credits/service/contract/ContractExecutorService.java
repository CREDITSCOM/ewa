package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;

public interface ContractExecutorService {
    void execute(String address, String methodName, String[] params) throws ContractExecutorException;
}

package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;

public interface ContractExecutorService {
    /**
     * Executes a default constructor by specified address
     * @param address A contract address
     * @throws ContractExecutorException
     */
    void execute(String address) throws ContractExecutorException;

    /**
     * Executes a method by specified address, method name and parameters.
     * It performs a default constructor to instantiate a class if necessary.
     * @param address A contract address
     * @param methodName A name of a method
     * @param params Parameters of a method
     * @throws ContractExecutorException
     */
    void execute(String address, String methodName, String[] params) throws ContractExecutorException;
}

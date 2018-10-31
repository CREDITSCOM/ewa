package com.credits.service.contract;

import com.credits.client.executor.pojo.MethodDescriptionData;
import com.credits.exception.ContractExecutorException;
import com.credits.general.thrift.generated.Variant;
import com.credits.thrift.ReturnValue;

import java.util.List;

public interface ContractExecutorService {
    /**
     * Executes a method by specified address, method name and parameters.
     * It performs a default constructor to instantiate a class if necessary.
     *
     * @param initiatorAddress    address of node that execute this method
     * @param methodName A name of a method
     * @param params     Parameters of a method
     */
//    void execute(String address, String methodName, String[] params) throws ContractExecutorException;


    ReturnValue execute( byte[] initiatorAddress, byte[] bytecode, byte[] contractState, String methodName, Variant[] params ) throws ContractExecutorException;

    List<MethodDescriptionData> getContractsMethods(byte[] contractBytecode) throws ContractExecutorException;
}

package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.thrift.MethodDescription;
import com.credits.thrift.ReturnValue;
import com.credits.thrift.generated.Variant;

import java.util.List;

public interface ContractExecutorService {
    /**
     * Executes a default constructor by specified address
     *
     * @param address A contract address
     * @throws ContractExecutorException
     */
//    void execute(String address, String specialProperty) throws ContractExecutorException;

    /**
     * Executes a method by specified address, method name and parameters.
     * It performs a default constructor to instantiate a class if necessary.
     *
     * @param initiatorAddress    address of node that execute this method
     * @param methodName A name of a method
     * @param params     Parameters of a method
     * @throws ContractExecutorException
     */
//    void execute(String address, String methodName, String[] params) throws ContractExecutorException;


    ReturnValue execute( byte[] initiatorAddress, byte[] bytecode, byte[] contractState, String methodName, Variant[] params )
        throws ContractExecutorException, LevelDbClientException;

    List<MethodDescription> getContractsMethods(byte[] contractBytecode) throws ContractExecutorException;
}

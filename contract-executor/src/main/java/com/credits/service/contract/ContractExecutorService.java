package com.credits.service.contract;

import com.credits.client.executor.pojo.MethodDescriptionData;
import com.credits.exception.CompilationException;
import com.credits.exception.ContractExecutorException;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.thrift.generated.Variant;
import com.credits.thrift.ReturnValue;

import java.util.List;
import java.util.Map;

public interface ContractExecutorService {

    /**
     * Executes a method by specified address, method name and parameters.
     * It performs a default constructor to instantiate a class if necessary.
     *
     * @param initiatorAddress    address of node that execute this method
     * @param methodName A name of a method
     * @param params     Parameters of a method
     */
    ReturnValue execute(byte[] initiatorAddress, byte[] bytecode, byte[] contractState, String methodName, Variant[][] params, long executionTime) throws ContractExecutorException;

    List<MethodDescriptionData> getContractsMethods(byte[] contractBytecode) throws ContractExecutorException;

    Map<String, Variant> getContractVariables(byte[] contractBytecode, byte[] contractState) throws ContractExecutorException;

    byte[] compileClass(String sourceCode) throws CompilationErrorException, CompilationException, ContractExecutorException, CompilationErrorException;
}

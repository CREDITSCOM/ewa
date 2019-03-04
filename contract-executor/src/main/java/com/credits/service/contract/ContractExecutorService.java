package com.credits.service.contract;

import com.credits.exception.CompilationException;
import com.credits.exception.ContractExecutorException;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.Variant;
import com.credits.pojo.apiexec.SmartContractGetResultData;
import com.credits.thrift.ReturnValue;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public interface ContractExecutorService {

    /**
     * Executes a method by specified address, method name and parameters.
     * It performs a default constructor to instantiate a class if necessary.
     *
     * @param accessId
     * @param initiatorAddress    address of node that execute this method
     * @param methodName A name of a method
     * @param params     Parameters of a method
     */
    ReturnValue execute(long accessId, byte[] initiatorAddress, byte[] contractAddress, List<ByteCodeObjectData> byteCodeObjectDataList, byte[] contractState, String methodName, Variant[][] params,
        long executionTime) throws ContractExecutorException;

    List<MethodDescriptionData> getContractsMethods(List<ByteCodeObjectData> byteCodeObjectDataList) throws ContractExecutorException;

    Map<String, Variant> getContractVariables(List<ByteCodeObjectData> contractBytecode, byte[] contractState) throws ContractExecutorException;

    List<ByteCodeObjectData> compileClass(String sourceCode) throws CompilationErrorException, CompilationException, ContractExecutorException, CompilationErrorException;

    ReturnValue executeExternalSmartContract(long accessId, String initiatorAddress,
        String externalSmartContractAddress, String externalSmartContractMethod,
        List<Object> externalSmartContractParams, SmartContractGetResultData externalSmartContractByteCode,
        Map<ByteBuffer, ByteBuffer> externalContractsStateByteCode);
}

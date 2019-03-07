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
import java.util.concurrent.ExecutionException;

public interface ContractExecutorService {

    ReturnValue deploySmartContract(Session session);

    ReturnValue executeSmartContract(InvokeMethodSession session) throws ContractExecutorException;

    List<MethodDescriptionData> getContractsMethods(List<ByteCodeObjectData> byteCodeObjectDataList) throws ContractExecutorException;

    Map<String, Variant> getContractVariables(List<ByteCodeObjectData> contractBytecode, byte[] contractState) throws ContractExecutorException;

    List<ByteCodeObjectData> compileClass(String sourceCode) throws CompilationErrorException, CompilationException, ContractExecutorException, CompilationErrorException;


    ReturnValue executeExternalSmartContract(long accessId, String initiatorAddress,
        String externalSmartContractAddress, String externalSmartContractMethod,
        List<Object> externalSmartContractParams, List<ByteCodeObjectData> byteCodeObjectDataList, byte[] contractState,
        Map<ByteBuffer, ByteBuffer> externalContractsStateByteCode) throws ExecutionException, InterruptedException;
}

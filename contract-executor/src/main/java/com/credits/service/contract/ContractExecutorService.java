package com.credits.service.contract;

import com.credits.exception.CompilationException;
import com.credits.exception.ContractExecutorException;
import com.credits.general.exception.CompilationErrorException;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.Variant;
import com.credits.pojo.apiexec.SmartContractGetResultData;
import com.credits.service.contract.session.DeployContractSession;
import com.credits.service.contract.session.InvokeMethodSession;
import com.credits.thrift.ReturnValue;

import java.util.List;
import java.util.Map;

public interface ContractExecutorService {

    ReturnValue deploySmartContract(DeployContractSession session);

    ReturnValue executeSmartContract(InvokeMethodSession session) throws ContractExecutorException;

    List<MethodDescriptionData> getContractsMethods(List<ByteCodeObjectData> byteCodeObjectDataList) throws ContractExecutorException;

    Map<String, Variant> getContractVariables(List<ByteCodeObjectData> contractBytecode, byte[] contractState) throws ContractExecutorException;

    List<ByteCodeObjectData> compileClass(String sourceCode) throws CompilationErrorException, CompilationException, ContractExecutorException, CompilationErrorException;

    ReturnValue executeExternalSmartContract(InvokeMethodSession session, Map<String, SmartContractGetResultData> externalSmartContracts);
}

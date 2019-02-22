package com.credits.client.executor.service;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.client.executor.thrift.generated.*;
import com.credits.general.pojo.VariantData;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.Variant;

import java.util.List;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public interface ContractExecutorThriftApi {

    ExecuteByteCodeResult executeByteCode(long accessId, byte[] initiatorAddress, byte[] contractAddress, List<ByteCodeObject> byteCodeObjects, byte[] objectState, String method, List<VariantData> params, long executionTime) throws ContractExecutorClientException;

    ExecuteByteCodeMultipleResult executeByteCodeMultiple(byte[] initiatorAddress, byte[] contractAddress, List<ByteCodeObject> byteCodeObjects, byte[] contractState, String method, List<List<Variant>> params, long executionTime);

    GetContractMethodsResult getContractMethods(List<ByteCodeObject> byteCodeObjects) throws ContractExecutorClientException;

    GetContractVariablesResult getContractVariables(List<ByteCodeObject> byteCodeObjects, byte[] contractState);

    CompileSourceCodeResult compileSourceCode(String sourceCode);
}

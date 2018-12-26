package com.credits.client.executor.service;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.client.executor.thrift.generated.CompileSourceCodeResult;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeMultipleResult;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.client.executor.thrift.generated.GetContractMethodsResult;
import com.credits.client.executor.thrift.generated.GetContractVariablesResult;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.Variant;

import java.util.List;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public interface ContractExecutorThriftApi {

    ExecuteByteCodeResult executeByteCode(byte[] address, List<ByteCodeObject> byteCodeObjects, byte[] objectState, String method, List<Variant> params, long executionTime) throws ContractExecutorClientException;

    ExecuteByteCodeMultipleResult executeByteCodeMultiple(byte[] address, List<ByteCodeObject> byteCodeObjects, byte[] contractState, String method, List<List<Variant>> params, long executionTime);

    GetContractMethodsResult getContractMethods(List<ByteCodeObject> byteCodeObjects) throws ContractExecutorClientException;

    GetContractVariablesResult getContractVariables(List<ByteCodeObject> byteCodeObjects, byte[] contractState);

    CompileSourceCodeResult compileSourceCode(String sourceCode);
}

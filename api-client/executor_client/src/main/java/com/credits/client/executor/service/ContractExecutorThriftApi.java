package com.credits.client.executor.service;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.client.executor.thrift.generated.*;
import com.credits.general.thrift.generated.Variant;
import org.apache.thrift.TException;

import java.util.List;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public interface ContractExecutorThriftApi {

    ExecuteByteCodeResult executeByteCode(byte[] address, byte[] bytecode, byte[] objectState, String method, List<Variant> params, long executionTime) throws ContractExecutorClientException;

    ExecuteByteCodeMultipleResult executeByteCodeMultiple(byte[] address, byte[] bytecode, byte[] contractState, String method, List<List<Variant>> params, long executionTime);

    GetContractMethodsResult getContractMethods(byte[] bytecode) throws ContractExecutorClientException;

    GetContractVariablesResult getContractVariables(byte[] byteCode, byte[] contractState);

    CompileSourceCodeResult compileSourceCode(String sourceCode);
}

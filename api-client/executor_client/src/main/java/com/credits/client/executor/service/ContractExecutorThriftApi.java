package com.credits.client.executor.service;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.client.executor.thrift.generated.GetContractMethodsResult;
import com.credits.general.thrift.generated.Variant;

import java.util.List;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public interface ContractExecutorThriftApi {

    ExecuteByteCodeResult executeContractMethod(byte[] address, byte[] bytecode, byte[] objectState, String method, List<Variant> params, long executionTime) throws ContractExecutorClientException;

    GetContractMethodsResult getContractMethods(byte[] address) throws ContractExecutorClientException;
}

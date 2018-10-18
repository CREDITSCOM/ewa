package com.credits.client.executor.service;

import com.credits.client.executor.exception.ExecutorClientException;
import com.credits.client.executor.thrift.APIResponse;
import com.credits.client.executor.thrift.GetContractMethodsResult;
import com.credits.general.thrift.generate.Variant;

import java.util.List;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public interface ContractExecutorThriftApi {

    APIResponse executeContractMethod(byte[] address, byte[] bytecode, byte[] objectState, String method, List<Variant> params) throws ExecutorClientException;

    GetContractMethodsResult getContractMethods(byte[] address) throws ExecutorClientException;
}

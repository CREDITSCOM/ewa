package com.credits.client.executor.service;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.client.executor.thrift.GetContractMethodsResult;
import com.credits.general.thrift.generate.APIResponse;
import com.credits.general.thrift.generate.Variant;

import java.util.List;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public interface ContractExecutorThriftApi {

    APIResponse executeContractMethod(byte[] address, byte[] bytecode, byte[] objectState, String method, List<Variant> params) throws ContractExecutorClientException;

    GetContractMethodsResult getContractMethods(byte[] address) throws ContractExecutorClientException;
}

package com.credits.client.executor.service;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.client.executor.pojo.ExecuteResponseData;
import com.credits.general.thrift.generated.Variant;

import java.util.List;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public interface ContractExecutorApiService {

    ExecuteResponseData executeContractMethod(byte[] address, byte[] bytecode, byte[] objectState, String methodName, List<Variant> params, long executionTime)
        throws ContractExecutorClientException;
}

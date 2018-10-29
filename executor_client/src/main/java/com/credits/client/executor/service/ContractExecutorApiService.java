package com.credits.client.executor.service;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.thrift.generate.Variant;

import java.util.List;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public interface ContractExecutorApiService {

    ApiResponseData executeContractMethod(byte[] address, byte[] bytecode, byte[] objectState, String methodName,
                                          List<Variant> params) throws ContractExecutorClientException;
}

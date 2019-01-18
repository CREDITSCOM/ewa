package com.credits.client.executor.service;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.pojo.VariantData;

import java.util.List;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public interface ContractExecutorApiService {

    ExecuteByteCodeResult executeContractMethod(byte[] initiatorAddress, byte[] contractAddress, List<ByteCodeObject> byteCodeObjects, byte[] objectState, String methodName, List<VariantData> params, long executionTime)
        throws ContractExecutorClientException;
}

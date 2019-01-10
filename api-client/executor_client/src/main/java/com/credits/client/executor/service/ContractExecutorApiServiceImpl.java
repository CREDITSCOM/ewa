package com.credits.client.executor.service;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.general.thrift.generated.Variant;

import java.util.List;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public class ContractExecutorApiServiceImpl implements ContractExecutorApiService {

    private static ContractExecutorApiServiceImpl instance;
    private final ContractExecutorThriftApiClient apiClient;

    private ContractExecutorApiServiceImpl(String host, int port) {
        apiClient = ContractExecutorThriftApiClient.getInstance(host, port);
    }

    public static ContractExecutorApiServiceImpl getInstance(String host, Integer port) {
        ContractExecutorApiServiceImpl localInstance = instance;
        if (localInstance == null) {
            synchronized (ContractExecutorApiServiceImpl.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ContractExecutorApiServiceImpl(host, port);
                }
            }
        }
        return localInstance;
    }

    @Override
    public ExecuteByteCodeResult executeContractMethod(byte[] address, List<ByteCodeObject> byteCodeObjects, byte[] objectState, String methodName, List<Variant> params, long executionTime)
        throws ContractExecutorClientException {
        ExecuteByteCodeResult
            result = apiClient.executeByteCode(address, byteCodeObjects, objectState, methodName, params, executionTime);
        return result;
    }
}

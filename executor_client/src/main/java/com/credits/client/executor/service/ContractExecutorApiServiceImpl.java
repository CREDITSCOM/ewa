package com.credits.client.executor.service;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.thrift.generate.APIResponse;
import com.credits.general.util.Converter;

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
            synchronized (ContractExecutorThriftApiClient.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ContractExecutorApiServiceImpl(host, port);
                }
            }
        }
        return localInstance;
    }

    @Override
    public ApiResponseData executeContractMethod(SmartContractData smartContractData) throws ContractExecutorClientException {
        if (smartContractData != null) {
            APIResponse apiResponse =
                apiClient.executeContractMethod(smartContractData.getAddress(),
                    smartContractData.getByteCode(),
                    smartContractData.getObjectState(),
                    smartContractData.getMethod(),
                    smartContractData.getParams());
            return Converter.apiResponseToApiResponseData(apiResponse);

        } else {
            throw new NullPointerException("SmartContractData is null");
        }
    }
}

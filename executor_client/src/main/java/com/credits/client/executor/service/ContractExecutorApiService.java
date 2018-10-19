package com.credits.client.executor.service;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.SmartContractData;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public interface ContractExecutorApiService {

    ApiResponseData executeContractMethod(SmartContractData smartContractData) throws ContractExecutorClientException;

}

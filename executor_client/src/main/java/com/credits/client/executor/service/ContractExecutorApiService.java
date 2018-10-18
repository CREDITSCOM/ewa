package com.credits.client.executor.service;

import com.credits.client.executor.pojo.ApiResponseData;
import com.credits.general.exception.CreditsGeneralException;
import com.credits.general.thrift.generate.Variant;
import com.credits.general.util.Converter;

import java.util.List;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public interface ContractExecutorApiService {

    ApiResponseData executeContractMethod(byte[] address, byte[] bytecode, byte[] objectState, String method, List<Variant> params);

    default ApiResponseData executeContractMethod(String addressBase58, byte[] bytecode, byte[] objectState, String method, List<Variant> params) throws CreditsGeneralException {
        return executeContractMethod(Converter.decodeFromBASE58(addressBase58), bytecode, objectState, method, params);
    }
}

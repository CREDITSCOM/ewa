package com.credits.client.executor.util;

import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.pojo.ApiResponseData;

// TODO refactor
public class ContractExecutorPojoConverter {

    public static ApiResponseData apiResponseToApiResponseData(APIResponse apiResponse) {
        return new ApiResponseData(ApiResponseCode.valueOf(apiResponse.getCode()), apiResponse.getMessage(), null/* TODO refactor apiResponse.ret_val*/);
    }
}

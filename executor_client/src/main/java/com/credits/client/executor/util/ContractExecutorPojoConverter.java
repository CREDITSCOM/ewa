package com.credits.client.executor.util;

import com.credits.client.executor.thrift.APIResponse;
import com.credits.general.pojo.ApiResponseData;

public class ContractExecutorPojoConverter {

    public static ApiResponseData apiResponseToApiResponseData(APIResponse apiResponse) {
        return new ApiResponseData(apiResponse.getCode(), apiResponse.getMessage(), apiResponse.ret_val);
    }
}

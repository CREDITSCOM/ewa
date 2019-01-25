package com.credits.client.executor.util;

import com.credits.client.executor.pojo.ExecuteResponseData;
import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.thrift.generated.ExecuteByteCodeResult;

public class ContractExecutorPojoConverter {

    public static ExecuteResponseData executeByteCodeResultToExecuteResponseData(ExecuteByteCodeResult result) {
        return new ExecuteResponseData(
            new ApiResponseData(ApiResponseCode.valueOf(result.getStatus().getCode()), result.getStatus().getMessage()),
            result.getRet_val());
    }
}

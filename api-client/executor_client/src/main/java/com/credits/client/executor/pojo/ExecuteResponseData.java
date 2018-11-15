package com.credits.client.executor.pojo;

import com.credits.general.pojo.ApiResponseData;
import com.credits.general.thrift.generated.Variant;

public class ExecuteResponseData extends ApiResponseData {
    private Variant executeBytecodeResult;

    public ExecuteResponseData(ApiResponseData response, Variant returnValue) {
        super(response.getCode(), response.getMessage());
        executeBytecodeResult = returnValue;
    }
}

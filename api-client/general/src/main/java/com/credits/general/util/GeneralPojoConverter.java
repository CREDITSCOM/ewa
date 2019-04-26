package com.credits.general.util;

import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.thrift.generated.APIResponse;

public class GeneralPojoConverter {

    public static ApiResponseData createApiResponseData(APIResponse thriftStruct) {
        return new ApiResponseData(ApiResponseCode.valueOf(thriftStruct.getCode()), thriftStruct.getMessage());
    }

    public static APIResponse createApiResponse(ApiResponseData data) {
        return new APIResponse((byte)data.getCode().code, data.getMessage());
    }

}

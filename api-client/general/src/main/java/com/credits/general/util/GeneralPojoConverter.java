package com.credits.general.util;

import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.ExecuteByteCodeResultData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.ExecuteByteCodeResult;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.variant.VariantConverter;

import java.nio.ByteBuffer;

public class GeneralPojoConverter {

    public static ApiResponseData createApiResponseData(APIResponse thriftStruct) {
        return new ApiResponseData(ApiResponseCode.valueOf(thriftStruct.getCode()), thriftStruct.getMessage());
    }

    public static APIResponse createApiResponse(ApiResponseData data) {
        return new APIResponse((byte)data.getCode().code, data.getMessage());
    }

    public static ExecuteByteCodeResultData createExecuteByteCodeResultData(ExecuteByteCodeResult result) {
        return new ExecuteByteCodeResultData(createApiResponseData(result.getStatus()),
                result.contractState.array(),
                (result.getRet_val() == null ? null :VariantConverter.variantToVariantData(result.getRet_val()))
        );
    }

    public static ExecuteByteCodeResult createExecuteByteCodeResult(ExecuteByteCodeResultData data) {
        ExecuteByteCodeResult thriftSrtuct = new ExecuteByteCodeResult(
            createApiResponse(data),
            ByteBuffer.wrap(data.getContractState())
        );
        Variant retVal = (data.getRetVal() == null ? null : VariantConverter.variantDataToVariant(data.getRetVal()));
        thriftSrtuct.setRet_val(retVal);
        return thriftSrtuct;
    }
}
